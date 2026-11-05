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
	private static final Pattern CREATE_TABLE = Pattern.compile(
			"CREATE\\s+TABLE\\s+(\\w+)\\s*\\((.*?)\\)\\s*(ENGINE[^;]*);", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern CUALQUIER_CREATE_TABLE = Pattern.compile("CREATE\\s+TABLE", Pattern.CASE_INSENSITIVE);
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
		int declaradas = 0;
		for (Path script : scripts) {
			String sql = sinComentarios(leer(script));
			Matcher todas = CUALQUIER_CREATE_TABLE.matcher(sql);
			while (todas.find()) {
				declaradas++;
			}
			Matcher m = CREATE_TABLE.matcher(sql);
			while (m.find()) {
				tablas++;
				String nombre = m.group(1);
				String columnas = m.group(2).toUpperCase(Locale.ROOT);
				String opciones = m.group(3).toUpperCase(Locale.ROOT);
				assertThat(columnas).as("PRIMARY KEY en " + nombre).contains("PRIMARY KEY");
				assertThat(opciones).as("ENGINE=InnoDB en " + nombre).contains("ENGINE=INNODB");
				assertThat(opciones).as("utf8mb4 en " + nombre).contains("UTF8MB4");
			}
		}
		assertThat(tablas).as("tablas definidas").isGreaterThanOrEqualTo(5);
		assertThat(tablas).as("toda tabla declara ENGINE y charset").isEqualTo(declaradas);
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
		Pattern insertUsuario = Pattern.compile("INSERT\\s+INTO\\s+usuario\\b(.*?);",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Pattern fila = Pattern.compile("\\(\\s*\\d+\\s*,\\s*'[^']*'\\s*,\\s*'[^']*'\\s*,\\s*'([^']*)'");
		for (Path script : scripts) {
			Matcher bloque = insertUsuario.matcher(leer(script));
			while (bloque.find()) {
				Matcher valores = fila.matcher(bloque.group(1));
				while (valores.find()) {
					assertThat(valores.group(1)).as("contrasena en " + script)
							.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
				}
			}
		}
	}
}
