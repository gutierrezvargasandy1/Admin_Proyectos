package com.mx.cbtis.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Pruebas de integracion de la base de datos contra un MariaDB REAL.
 *
 * Por cada ejecucion se crea una base de datos TEMPORAL (gestor_ci_xxxx), se le aplican
 * las migraciones con Flyway, se ejecutan las pruebas y al final se elimina.
 * Asi el pipeline nunca toca la base de datos de trabajo (db_cbtis).
 *
 * Variables de entorno:
 *   DB_HOST (por defecto 127.0.0.1), DB_PORT (por defecto 3307),
 *   DB_ADMIN_USER (por defecto root), DB_ADMIN_PASSWORD (obligatoria).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EsquemaBaseDatosTest {

	private static final String CONTRASENA_DEMO = "Demo1234";

	private final String host = variable("DB_HOST", "127.0.0.1");
	private final String puerto = variable("DB_PORT", "3307");
	private final String usuario = variable("DB_ADMIN_USER", "root");
	private final String clave = System.getenv("DB_ADMIN_PASSWORD");
	private final String baseDatos = "gestor_ci_" + Long.toString(System.currentTimeMillis(), 36);

	private MigrateResult resultadoMigracion;
	private Flyway flyway;

	private static String variable(String nombre, String porDefecto) {
		String valor = System.getenv(nombre);
		return valor == null || valor.isBlank() ? porDefecto : valor;
	}

	private String urlServidor() {
		return "jdbc:mariadb://" + host + ":" + puerto + "/";
	}

	private Connection conexion() throws SQLException {
		return DriverManager.getConnection(urlServidor() + baseDatos, usuario, clave);
	}

	@BeforeAll
	void crearBaseTemporalYMigrar() throws SQLException {
		assertThat(clave).as("Definir la variable de entorno DB_ADMIN_PASSWORD").isNotBlank();
		try (Connection admin = DriverManager.getConnection(urlServidor(), usuario, clave);
				Statement st = admin.createStatement()) {
			st.execute("CREATE DATABASE " + baseDatos + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
		}
		flyway = Flyway.configure()
				.dataSource(urlServidor() + baseDatos, usuario, clave)
				.locations("classpath:db/migration")
				.cleanDisabled(true)
				.load();
		resultadoMigracion = flyway.migrate();
	}

	@AfterAll
	void eliminarBaseTemporal() throws SQLException {
		try (Connection admin = DriverManager.getConnection(urlServidor(), usuario, clave);
				Statement st = admin.createStatement()) {
			st.execute("DROP DATABASE IF EXISTS " + baseDatos);
		}
	}

	// ------------------------------------------------------------ utilidades

	private int entero(String sql, Object... parametros) throws SQLException {
		try (Connection c = conexion(); PreparedStatement ps = c.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	private List<String> textos(String sql, Object... parametros) throws SQLException {
		List<String> resultado = new ArrayList<>();
		try (Connection c = conexion(); PreparedStatement ps = c.prepareStatement(sql)) {
			for (int i = 0; i < parametros.length; i++) {
				ps.setObject(i + 1, parametros[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					resultado.add(rs.getString(1));
				}
			}
		}
		return resultado;
	}

	private void ejecutar(String sql) throws SQLException {
		try (Connection c = conexion(); Statement st = c.createStatement()) {
			st.executeUpdate(sql);
		}
	}

	// ------------------------------------------------------------ migraciones

	@Test
	@DisplayName("Flyway aplica todas las migraciones sin errores")
	void migracionesAplicadas() {
		assertThat(resultadoMigracion.success).isTrue();
		assertThat(resultadoMigracion.migrationsExecuted).isEqualTo(2);

		MigrationInfo[] aplicadas = flyway.info().applied();
		assertThat(aplicadas).hasSize(2);
		for (MigrationInfo migracion : aplicadas) {
			assertThat(migracion.getState()).isEqualTo(MigrationState.SUCCESS);
		}
		flyway.validate();
	}

	// ------------------------------------------------------------ estructura

	@Test
	@DisplayName("Existen las 5 tablas de negocio y la de secuencia de Hibernate")
	void tablasExisten() throws SQLException {
		List<String> tablas = textos(
				"SELECT table_name FROM information_schema.tables WHERE table_schema = ?", baseDatos);

		assertThat(tablas).contains("usuario", "proyectos", "tareas", "comentarios", "asignaciones",
				"hibernate_sequence", "flyway_schema_history");
	}

	@Test
	@DisplayName("Todas las tablas usan InnoDB y utf8mb4")
	void motorYCharset() throws SQLException {
		assertThat(entero("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? "
				+ "AND table_name <> 'flyway_schema_history' "
				+ "AND (engine <> 'InnoDB' OR table_collation NOT LIKE 'utf8mb4%')", baseDatos)).isZero();
	}

	@Test
	@DisplayName("Existen las 4 llaves foraneas con borrado en cascada")
	void llavesForaneas() throws SQLException {
		List<String> llaves = textos("SELECT CONCAT(table_name, '->', referenced_table_name, ':', delete_rule) "
				+ "FROM information_schema.referential_constraints WHERE constraint_schema = ?", baseDatos);

		assertThat(llaves).containsExactlyInAnyOrder(
				"tareas->proyectos:CASCADE",
				"comentarios->tareas:CASCADE",
				"asignaciones->usuario:CASCADE",
				"asignaciones->tareas:CASCADE");
	}

	// ------------------------------------------------------------ datos semilla

	@Test
	@DisplayName("Los datos semilla se cargaron completos")
	void datosSemilla() throws SQLException {
		assertThat(entero("SELECT COUNT(*) FROM usuario")).isEqualTo(3);
		assertThat(entero("SELECT COUNT(*) FROM proyectos")).isEqualTo(2);
		assertThat(entero("SELECT COUNT(*) FROM tareas")).isEqualTo(4);
		assertThat(entero("SELECT COUNT(*) FROM comentarios")).isEqualTo(3);
		assertThat(entero("SELECT COUNT(*) FROM asignaciones")).isEqualTo(2);
	}

	@Test
	@DisplayName("Las contrasenas estan cifradas con BCrypt y validan con la contrasena demo")
	void contrasenasCifradas() throws SQLException {
		BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
		List<String> hashes = textos("SELECT contrasena FROM usuario");

		assertThat(hashes).isNotEmpty();
		for (String hash : hashes) {
			assertThat(hash).matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
			assertThat(bcrypt.matches(CONTRASENA_DEMO, hash)).isTrue();
			assertThat(bcrypt.matches("otraContrasena", hash)).isFalse();
		}
	}

	@Test
	@DisplayName("No hay registros huerfanos")
	void sinHuerfanos() throws SQLException {
		assertThat(entero("SELECT COUNT(*) FROM tareas t LEFT JOIN proyectos p "
				+ "ON t.id_proyecto = p.id_proyecto WHERE p.id_proyecto IS NULL")).isZero();
		assertThat(entero("SELECT COUNT(*) FROM comentarios c LEFT JOIN tareas t "
				+ "ON c.id_tarea = t.id_tarea WHERE t.id_tarea IS NULL")).isZero();
		assertThat(entero("SELECT COUNT(*) FROM asignaciones a LEFT JOIN usuario u "
				+ "ON a.id_usuario = u.id_usuario WHERE u.id_usuario IS NULL")).isZero();
	}

	@Test
	@DisplayName("La secuencia de Hibernate inicia despues de los ids semilla")
	void secuenciaHibernate() throws SQLException {
		int siguiente = entero("SELECT next_val FROM hibernate_sequence");

		assertThat(siguiente).isGreaterThan(entero("SELECT MAX(id_usuario) FROM usuario"));
		assertThat(siguiente).isGreaterThan(entero("SELECT MAX(id_tarea) FROM tareas"));
	}

	@Test
	@DisplayName("No hay datos reales: todos los correos son de @ejemplo.com")
	void soloDatosFicticios() throws SQLException {
		assertThat(entero("SELECT COUNT(*) FROM usuario WHERE correo_electronico NOT LIKE '%@ejemplo.com'")).isZero();
	}

	// ------------------------------------------------------------ restricciones (casos de fracaso)

	@Test
	@DisplayName("Rechaza una tarea de un proyecto que no existe (llave foranea)")
	void rechazaTareaHuerfana() {
		assertThatThrownBy(() -> ejecutar(
				"INSERT INTO tareas (nombre, id_proyecto) VALUES ('Huerfana', 99999)"))
				.isInstanceOf(SQLException.class)
				.satisfies(e -> assertThat(((SQLException) e).getSQLState()).startsWith("23"));
	}

	@Test
	@DisplayName("Rechaza un correo electronico duplicado (UNIQUE)")
	void rechazaCorreoDuplicado() {
		assertThatThrownBy(() -> ejecutar("INSERT INTO usuario (nombre, correo_electronico, contrasena) "
				+ "VALUES ('Copia', 'ana.demo@ejemplo.com', '$2a$10$x')"))
				.isInstanceOf(SQLException.class)
				.satisfies(e -> assertThat(((SQLException) e).getSQLState()).startsWith("23"));
	}

	@Test
	@DisplayName("Rechaza un usuario sin contrasena (NOT NULL)")
	void rechazaUsuarioSinContrasena() {
		assertThatThrownBy(() -> ejecutar("INSERT INTO usuario (nombre, correo_electronico, contrasena) "
				+ "VALUES ('Sin clave', 'sin.clave@ejemplo.com', NULL)"))
				.isInstanceOf(SQLException.class)
				.satisfies(e -> assertThat(((SQLException) e).getSQLState()).startsWith("23"));
	}

	@Test
	@DisplayName("Al borrar un proyecto se borran en cascada sus tareas y comentarios")
	void borradoEnCascada() throws SQLException {
		ejecutar("INSERT INTO proyectos (id_proyecto, nombre) VALUES (500, 'Temporal')");
		ejecutar("INSERT INTO tareas (id_tarea, nombre, id_proyecto) VALUES (501, 'Temporal', 500)");
		ejecutar("INSERT INTO comentarios (texto_comentario, id_tarea) VALUES ('Temporal', 501)");

		ejecutar("DELETE FROM proyectos WHERE id_proyecto = 500");

		assertThat(entero("SELECT COUNT(*) FROM tareas WHERE id_proyecto = 500")).isZero();
		assertThat(entero("SELECT COUNT(*) FROM comentarios WHERE id_tarea = 501")).isZero();
	}
}
