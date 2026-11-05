package com.mx.cbtis.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Analisis estatico (lint) de los scripts de migracion SQL.
 * No necesita base de datos: revisa reglas de calidad y seguridad del codigo SQL
 * antes de ejecutarlo. Corre en la etapa "Analisis estatico SQL" del pipeline.
 */
class ScriptsSqlLintTest {

	private static final Path CARPETA = Path.of("src", "main", "resources", "db", "migration");
	private static final Pattern NOMBRE_MIGRACION = Pattern.compile("^V(\\d+)__[a-z0-9_]+\\.sql$");
	private static final Pattern HASH_BCRYPT = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
	private static final Pattern CORREO = Pattern.compile("'[^'@\\s]+@([^'\\s]+)'");

	private static List<Path> scripts;

	@BeforeAll
	static void cargarScripts() throws IOException {
		try (Stream<Path> archivos = Files.list(CARPETA)) {
			scripts = archivos.filter(p -> p.toString().endsWith(".sql")).sorted().toList();
		}
	}

	private static String leer(Path script) throws IOException {
		return Files.readString(script, StandardCharsets.UTF_8);
	}

	/** Quita los comentarios "-- ..." para que no cuenten en las reglas. */
	private static String sinComentarios(String sql) {
		return sql.replaceAll("(?m)--.*$", "");
	}

	/**
	 * Separa un script en sentencias usando ';' (sin expresiones regulares complejas,
	 * para evitar backtracking). Los scripts no usan ';' dentro de textos.
	 */
	private static List<String> sentencias(String sql) {
		List<String> resultado = new ArrayList<>();
		for (String parte : sinComentarios(sql).split(";")) {
			String sentencia = parte.strip();
			if (!sentencia.isEmpty()) {
				resultado.add(sentencia);
			}
		}
		return resultado;
	}

	private static boolean empiezaCon(String sentencia, String prefijo) {
		return sentencia.toUpperCase(Locale.ROOT).replaceAll("\\s+", " ").startsWith(prefijo);
	}

	@Test
	@DisplayName("Existen migraciones y siguen la convencion de Flyway V<n>__descripcion.sql")
	void nombresDeMigracion() {
		assertThat(scripts).isNotEmpty();
		for (Path script : scripts) {
			assertThat(script.getFileName().toString()).matches(NOMBRE_MIGRACION);
		}
	}

	@Test
	@DisplayName("Las versiones son consecutivas (1, 2, 3...) sin huecos ni repetidos")
	void versionesConsecutivas() {
		List<Integer> versiones = new ArrayList<>();
		for (Path script : scripts) {
			Matcher m = NOMBRE_MIGRACION.matcher(script.getFileName().toString());
			assertThat(m.matches()).isTrue();
			versiones.add(Integer.parseInt(m.group(1)));
		}
		versiones.sort(null);
		for (int i = 0; i < versiones.size(); i++) {
			assertThat(versiones.get(i)).isEqualTo(i + 1);
		}
	}

	@Test
	@DisplayName("Ninguna migracion borra o vacia objetos (DROP / TRUNCATE)")
	void sinSentenciasDestructivas() throws IOException {
		for (Path script : scripts) {
			String sql = sinComentarios(leer(script)).toUpperCase(Locale.ROOT);
			assertThat(sql).as(script.toString())
					.doesNotContain("DROP DATABASE")
					.doesNotContain("DROP TABLE")
					.doesNotContain("TRUNCATE");
		}
	}

	@Test
	@DisplayName("No se usa SELECT * ni el charset latin1")
	void sinMalasPracticas() throws IOException {
		for (Path script : scripts) {
			String sql = sinComentarios(leer(script)).toUpperCase(Locale.ROOT);
			assertThat(sql).as(script.toString())
					.doesNotContain("SELECT *")
					.doesNotContain("LATIN1");
		}
	}

	@Test
	@DisplayName("Cada tabla tiene PRIMARY KEY, motor InnoDB y charset utf8mb4")
	void tablasBienDefinidas() throws IOException {
		int tablas = 0;
		for (Path script : scripts) {
			for (String sentencia : sentencias(leer(script))) {
				if (empiezaCon(sentencia, "CREATE TABLE")) {
					tablas++;
					String mayusculas = sentencia.toUpperCase(Locale.ROOT);
					assertThat(mayusculas).as(sentencia.lines().findFirst().orElse(""))
							.contains("PRIMARY KEY")
							.contains("ENGINE=INNODB")
							.contains("UTF8MB4");
				}
			}
		}
		assertThat(tablas).as("tablas definidas").isGreaterThanOrEqualTo(5);
	}

	@Test
	@DisplayName("Los datos semilla son ficticios: todos los correos son @ejemplo.com")
	void soloDatosFicticios() throws IOException {
		for (Path script : scripts) {
			Matcher m = CORREO.matcher(leer(script));
			while (m.find()) {
				assertThat(m.group(1)).as("dominio de correo en " + script).isEqualTo("ejemplo.com");
			}
		}
	}

	@Test
	@DisplayName("Las contrasenas de los datos semilla estan cifradas con BCrypt")
	void contrasenasCifradasEnScripts() throws IOException {
		int revisadas = 0;
		for (Path script : scripts) {
			for (String sentencia : sentencias(leer(script))) {
				if (empiezaCon(sentencia, "INSERT INTO USUARIO")) {
					for (String valor : textosEntreComillas(sentencia)) {
						if (valor.startsWith("$")) {
							revisadas++;
							assertThat(valor).as("contrasena en " + script).matches(HASH_BCRYPT);
						}
					}
				}
			}
		}
		assertThat(revisadas).as("contrasenas revisadas").isPositive();
	}

	/** Extrae los textos entre comillas simples recorriendo caracter por caracter. */
	private static List<String> textosEntreComillas(String sentencia) {
		List<String> textos = new ArrayList<>();
		int inicio = sentencia.indexOf('\'');
		while (inicio >= 0) {
			int fin = sentencia.indexOf('\'', inicio + 1);
			if (fin < 0) {
				break;
			}
			textos.add(sentencia.substring(inicio + 1, fin));
			inicio = sentencia.indexOf('\'', fin + 1);
		}
		return textos;
	}
}