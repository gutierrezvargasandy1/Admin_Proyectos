package com.mx.cbtis.gestor.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.modelo.Usuario;

/**
 * Pruebas de los DTO de entrada (records de Java 21):
 * lectura del JSON en snake_case y conversion a entidad.
 */
class DtosTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	@DisplayName("ProyectoRequest: lee JSON snake_case y lo convierte a entidad")
	void proyectoDesdeJson() throws Exception {
		ProyectoRequest request = mapper.readValue("{\"id_proyecto\":197,\"nombre\":\"My API Rest\","
				+ "\"descripcion\":\"API\",\"fecha_inicio\":\"2026-05-31\",\"fecha_finalizacion\":\"2026-06-29\","
				+ "\"estado_proyecto\":\"En proceso\",\"fondo\":\"/Images/a.gif\"}", ProyectoRequest.class);

		Proyectos entidad = request.toEntity();

		assertThat(entidad.getIdProyecto()).isEqualTo(197);
		assertThat(entidad.getFechaInicio()).isEqualTo("2026-05-31");
		assertThat(entidad.getEstadoProyecto()).isEqualTo("En proceso");
		assertThat(entidad.getFondo()).isEqualTo("/Images/a.gif");
	}

	@Test
	@DisplayName("UsuarioRequest: lee JSON snake_case y lo convierte a entidad")
	void usuarioDesdeJson() throws Exception {
		UsuarioRequest request = mapper.readValue("{\"nombre\":\"Ana\",\"correo_electronico\":\"ana@cbtis.mx\","
				+ "\"contrasena\":\"Secreta123\",\"rol\":\"Estudiante\",\"imagen_usuario\":\"#70FAB7\"}",
				UsuarioRequest.class);

		Usuario entidad = request.toEntity();

		assertThat(entidad.getIdUsuario()).isNull();
		assertThat(entidad.getCorreoElectronico()).isEqualTo("ana@cbtis.mx");
		assertThat(entidad.getContrasena()).isEqualTo("Secreta123");
		assertThat(entidad.getImagenUsuario()).isEqualTo("#70FAB7");
	}

	@Test
	@DisplayName("Filtros y credenciales: leen su llave en snake_case")
	void filtrosDesdeJson() throws Exception {
		assertThat(mapper.readValue("{\"id_proyecto\":197}", FiltroProyectoRequest.class).idProyecto()).isEqualTo(197);
		assertThat(mapper.readValue("{\"id_tarea\":199}", FiltroTareaRequest.class).idTarea()).isEqualTo(199);

		CredencialesRequest credenciales = mapper.readValue(
				"{\"correo_electronico\":\"ana@cbtis.mx\",\"contrasena\":\"x\"}", CredencialesRequest.class);
		assertThat(credenciales.correoElectronico()).isEqualTo("ana@cbtis.mx");
		assertThat(credenciales.contrasena()).isEqualTo("x");
	}
}
