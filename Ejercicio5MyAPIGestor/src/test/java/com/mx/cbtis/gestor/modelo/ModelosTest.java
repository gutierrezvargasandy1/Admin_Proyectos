package com.mx.cbtis.gestor.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Pruebas de las entidades: constructores, getters/setters y el contrato JSON
 * (las llaves siguen en snake_case para no romper el frontend).
 */
class ModelosTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	@DisplayName("Asignaciones: constructor completo, constructor vacio y setters")
	void asignaciones() {
		Asignaciones completa = new Asignaciones(1, 2, 3, "2026-11-05");
		assertThat(completa.getIdAsignacion()).isEqualTo(1);
		assertThat(completa.getIdUsuario()).isEqualTo(2);
		assertThat(completa.getIdTarea()).isEqualTo(3);
		assertThat(completa.getFechaAsignacion()).isEqualTo("2026-11-05");

		Asignaciones vacia = new Asignaciones();
		assertThat(vacia.getIdAsignacion()).isNull();
		vacia.setIdAsignacion(10);
		vacia.setIdUsuario(20);
		vacia.setIdTarea(30);
		vacia.setFechaAsignacion("2026-12-01");
		assertThat(vacia.getIdAsignacion()).isEqualTo(10);
		assertThat(vacia.getIdUsuario()).isEqualTo(20);
		assertThat(vacia.getIdTarea()).isEqualTo(30);
		assertThat(vacia.getFechaAsignacion()).isEqualTo("2026-12-01");
	}

	@Test
	@DisplayName("Comentarios: constructor completo, constructor vacio y setters")
	void comentarios() {
		Comentarios completo = new Comentarios(1, "Ana Lopez", 3, "Subirlo hoy", "2026-11-05 10:00");
		assertThat(completo.getIdComentario()).isEqualTo(1);
		assertThat(completo.getIdUsuario()).isEqualTo("Ana Lopez");
		assertThat(completo.getIdTarea()).isEqualTo(3);
		assertThat(completo.getTextoComentario()).isEqualTo("Subirlo hoy");
		assertThat(completo.getFechaHora()).isEqualTo("2026-11-05 10:00");

		Comentarios vacio = new Comentarios();
		assertThat(vacio.getIdComentario()).isNull();
		vacio.setIdComentario(10);
		vacio.setIdUsuario("Luis Perez");
		vacio.setIdTarea(30);
		vacio.setTextoComentario("Listo");
		vacio.setFechaHora("2026-12-01 09:30");
		assertThat(vacio.getIdComentario()).isEqualTo(10);
		assertThat(vacio.getIdUsuario()).isEqualTo("Luis Perez");
		assertThat(vacio.getIdTarea()).isEqualTo(30);
		assertThat(vacio.getTextoComentario()).isEqualTo("Listo");
		assertThat(vacio.getFechaHora()).isEqualTo("2026-12-01 09:30");
	}

	@Test
	@DisplayName("Proyectos: constructor completo, constructor vacio y setters")
	void proyectos() {
		Proyectos completo = new Proyectos(1, "API", "Desc", "2026-01-01", "2026-06-30", "En proceso", "/fondo.png");
		assertThat(completo.getIdProyecto()).isEqualTo(1);
		assertThat(completo.getNombre()).isEqualTo("API");
		assertThat(completo.getDescripcion()).isEqualTo("Desc");
		assertThat(completo.getFechaInicio()).isEqualTo("2026-01-01");
		assertThat(completo.getFechaFinalizacion()).isEqualTo("2026-06-30");
		assertThat(completo.getEstadoProyecto()).isEqualTo("En proceso");
		assertThat(completo.getFondo()).isEqualTo("/fondo.png");

		Proyectos vacio = new Proyectos();
		assertThat(vacio.getIdProyecto()).isNull();
		vacio.setIdProyecto(10);
		vacio.setNombre("Web");
		vacio.setDescripcion("Otra");
		vacio.setFechaInicio("2026-02-01");
		vacio.setFechaFinalizacion("2026-07-31");
		vacio.setEstadoProyecto("Terminado");
		vacio.setFondo("/otro.png");
		assertThat(vacio.getIdProyecto()).isEqualTo(10);
		assertThat(vacio.getNombre()).isEqualTo("Web");
		assertThat(vacio.getDescripcion()).isEqualTo("Otra");
		assertThat(vacio.getFechaInicio()).isEqualTo("2026-02-01");
		assertThat(vacio.getFechaFinalizacion()).isEqualTo("2026-07-31");
		assertThat(vacio.getEstadoProyecto()).isEqualTo("Terminado");
		assertThat(vacio.getFondo()).isEqualTo("/otro.png");
	}

	@Test
	@DisplayName("Tareas: constructor completo, constructor vacio y setters")
	void tareas() {
		Tareas completa = new Tareas(1, "BD", "Tablas", "2026-06-13", "pendiente", "danger", 7);
		assertThat(completa.getIdTarea()).isEqualTo(1);
		assertThat(completa.getNombre()).isEqualTo("BD");
		assertThat(completa.getDescripcion()).isEqualTo("Tablas");
		assertThat(completa.getFechaVencimiento()).isEqualTo("2026-06-13");
		assertThat(completa.getLista()).isEqualTo("pendiente");
		assertThat(completa.getEstado()).isEqualTo("danger");
		assertThat(completa.getIdProyecto()).isEqualTo(7);

		Tareas vacia = new Tareas();
		assertThat(vacia.getIdTarea()).isNull();
		vacia.setIdTarea(10);
		vacia.setNombre("Front");
		vacia.setDescripcion("Vistas");
		vacia.setFechaVencimiento("2026-07-01");
		vacia.setLista("terminado");
		vacia.setEstado("success");
		vacia.setIdProyecto(8);
		assertThat(vacia.getIdTarea()).isEqualTo(10);
		assertThat(vacia.getNombre()).isEqualTo("Front");
		assertThat(vacia.getDescripcion()).isEqualTo("Vistas");
		assertThat(vacia.getFechaVencimiento()).isEqualTo("2026-07-01");
		assertThat(vacia.getLista()).isEqualTo("terminado");
		assertThat(vacia.getEstado()).isEqualTo("success");
		assertThat(vacia.getIdProyecto()).isEqualTo(8);
	}

	@Test
	@DisplayName("Usuario: constructor completo, constructor vacio y setters")
	void usuario() {
		Usuario completo = new Usuario(1, "Ana", "ana@cbtis.mx", "hash", "Estudiante", "#70FAB7");
		assertThat(completo.getIdUsuario()).isEqualTo(1);
		assertThat(completo.getNombre()).isEqualTo("Ana");
		assertThat(completo.getCorreoElectronico()).isEqualTo("ana@cbtis.mx");
		assertThat(completo.getContrasena()).isEqualTo("hash");
		assertThat(completo.getRol()).isEqualTo("Estudiante");
		assertThat(completo.getImagenUsuario()).isEqualTo("#70FAB7");

		Usuario vacio = new Usuario();
		assertThat(vacio.getIdUsuario()).isNull();
		vacio.setIdUsuario(10);
		vacio.setNombre("Luis");
		vacio.setCorreoElectronico("luis@cbtis.mx");
		vacio.setContrasena("otroHash");
		vacio.setRol("Docente");
		vacio.setImagenUsuario("#FFFFFF");
		assertThat(vacio.getIdUsuario()).isEqualTo(10);
		assertThat(vacio.getNombre()).isEqualTo("Luis");
		assertThat(vacio.getCorreoElectronico()).isEqualTo("luis@cbtis.mx");
		assertThat(vacio.getContrasena()).isEqualTo("otroHash");
		assertThat(vacio.getRol()).isEqualTo("Docente");
		assertThat(vacio.getImagenUsuario()).isEqualTo("#FFFFFF");
	}

	@Test
	@DisplayName("JSON: las llaves se conservan en snake_case (contrato con el frontend)")
	void contratoJsonSnakeCase() throws Exception {
		JsonNode tarea = mapper.valueToTree(new Tareas(1, "BD", "Tablas", "2026-06-13", "pendiente", "danger", 7));

		assertThat(tarea.has("id_tarea")).isTrue();
		assertThat(tarea.has("id_proyecto")).isTrue();
		assertThat(tarea.has("fecha_vencimiento")).isTrue();
		assertThat(tarea.has("idTarea")).isFalse();

		Proyectos leido = mapper.readValue(
				"{\"id_proyecto\":197,\"nombre\":\"My API Rest\",\"estado_proyecto\":\"En proceso\"}",
				Proyectos.class);
		assertThat(leido.getIdProyecto()).isEqualTo(197);
		assertThat(leido.getEstadoProyecto()).isEqualTo("En proceso");
	}

	@Test
	@DisplayName("JSON: la contrasena se puede recibir pero NUNCA se devuelve")
	void contrasenaEsSoloEscritura() throws Exception {
		Usuario leido = mapper.readValue(
				"{\"correo_electronico\":\"ana@cbtis.mx\",\"contrasena\":\"Secreta123\"}", Usuario.class);
		assertThat(leido.getContrasena()).isEqualTo("Secreta123");

		JsonNode json = mapper.valueToTree(leido);
		assertThat(json.has("contrasena")).isFalse();
		assertThat(json.get("correo_electronico").asText()).isEqualTo("ana@cbtis.mx");
	}
}
