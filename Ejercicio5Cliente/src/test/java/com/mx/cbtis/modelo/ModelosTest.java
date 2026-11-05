package com.mx.cbtis.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de los modelos del frontend: constructores, getters y setters.
 */
class ModelosTest {

	@Test
	@DisplayName("Comentarios: constructor completo, constructor vacio y setters")
	void comentarios() {
		Comentarios completo = new Comentarios(1, "Ana Lopez", 3, "Subirlo hoy", "2026-11-05 10:00");
		assertThat(completo.getId_comentario()).isEqualTo(1);
		assertThat(completo.getId_usuario()).isEqualTo("Ana Lopez");
		assertThat(completo.getId_tarea()).isEqualTo(3);
		assertThat(completo.getTexto_comentario()).isEqualTo("Subirlo hoy");
		assertThat(completo.getFecha_hora()).isEqualTo("2026-11-05 10:00");

		Comentarios vacio = new Comentarios();
		assertThat(vacio.getId_comentario()).isNull();
		vacio.setId_comentario(10);
		vacio.setId_usuario("Luis Perez");
		vacio.setId_tarea(30);
		vacio.setTexto_comentario("Listo");
		vacio.setFecha_hora("2026-12-01 09:30");
		assertThat(vacio.getId_comentario()).isEqualTo(10);
		assertThat(vacio.getId_usuario()).isEqualTo("Luis Perez");
		assertThat(vacio.getId_tarea()).isEqualTo(30);
		assertThat(vacio.getTexto_comentario()).isEqualTo("Listo");
		assertThat(vacio.getFecha_hora()).isEqualTo("2026-12-01 09:30");
	}

	@Test
	@DisplayName("Proyectos: constructor completo, constructor vacio y setters")
	void proyectos() {
		Proyectos completo = new Proyectos(1, "API", "Desc", "2026-01-01", "2026-06-30", "En proceso", "/fondo.png");
		assertThat(completo.getId_proyecto()).isEqualTo(1);
		assertThat(completo.getNombre()).isEqualTo("API");
		assertThat(completo.getDescripcion()).isEqualTo("Desc");
		assertThat(completo.getFecha_inicio()).isEqualTo("2026-01-01");
		assertThat(completo.getFecha_finalizacion()).isEqualTo("2026-06-30");
		assertThat(completo.getEstado_proyecto()).isEqualTo("En proceso");
		assertThat(completo.getFondo()).isEqualTo("/fondo.png");

		Proyectos vacio = new Proyectos();
		assertThat(vacio.getId_proyecto()).isNull();
		vacio.setId_proyecto(10);
		vacio.setNombre("Web");
		vacio.setDescripcion("Otra");
		vacio.setFecha_inicio("2026-02-01");
		vacio.setFecha_finalizacion("2026-07-31");
		vacio.setEstado_proyecto("Terminado");
		vacio.setFondo("/otro.png");
		assertThat(vacio.getId_proyecto()).isEqualTo(10);
		assertThat(vacio.getNombre()).isEqualTo("Web");
		assertThat(vacio.getDescripcion()).isEqualTo("Otra");
		assertThat(vacio.getFecha_inicio()).isEqualTo("2026-02-01");
		assertThat(vacio.getFecha_finalizacion()).isEqualTo("2026-07-31");
		assertThat(vacio.getEstado_proyecto()).isEqualTo("Terminado");
		assertThat(vacio.getFondo()).isEqualTo("/otro.png");
	}

	@Test
	@DisplayName("Tareas: constructor completo, constructor vacio y setters")
	void tareas() {
		Tareas completa = new Tareas(1, "BD", "Tablas", "2026-06-13", "pendiente", "danger", 7);
		assertThat(completa.getId_tarea()).isEqualTo(1);
		assertThat(completa.getNombre()).isEqualTo("BD");
		assertThat(completa.getDescripcion()).isEqualTo("Tablas");
		assertThat(completa.getFecha_vencimiento()).isEqualTo("2026-06-13");
		assertThat(completa.getLista()).isEqualTo("pendiente");
		assertThat(completa.getEstado()).isEqualTo("danger");
		assertThat(completa.getId_proyecto()).isEqualTo(7);

		Tareas vacia = new Tareas();
		assertThat(vacia.getId_tarea()).isNull();
		vacia.setId_tarea(10);
		vacia.setNombre("Front");
		vacia.setDescripcion("Vistas");
		vacia.setFecha_vencimiento("2026-07-01");
		vacia.setLista("terminado");
		vacia.setEstado("success");
		vacia.setId_proyecto(8);
		assertThat(vacia.getId_tarea()).isEqualTo(10);
		assertThat(vacia.getNombre()).isEqualTo("Front");
		assertThat(vacia.getDescripcion()).isEqualTo("Vistas");
		assertThat(vacia.getFecha_vencimiento()).isEqualTo("2026-07-01");
		assertThat(vacia.getLista()).isEqualTo("terminado");
		assertThat(vacia.getEstado()).isEqualTo("success");
		assertThat(vacia.getId_proyecto()).isEqualTo(8);
	}

	@Test
	@DisplayName("Usuario: constructor completo, constructor vacio y setters")
	void usuario() {
		Usuario completo = new Usuario(1, "Ana", "ana@cbtis.mx", "Secreta123", "Estudiante", "#70FAB7");
		assertThat(completo.getId_usuario()).isEqualTo(1);
		assertThat(completo.getNombre()).isEqualTo("Ana");
		assertThat(completo.getCorreo_electronico()).isEqualTo("ana@cbtis.mx");
		assertThat(completo.getContrasena()).isEqualTo("Secreta123");
		assertThat(completo.getRol()).isEqualTo("Estudiante");
		assertThat(completo.getImagen_usuario()).isEqualTo("#70FAB7");

		Usuario vacio = new Usuario();
		assertThat(vacio.getId_usuario()).isNull();
		vacio.setId_usuario(10);
		vacio.setNombre("Luis");
		vacio.setCorreo_electronico("luis@cbtis.mx");
		vacio.setContrasena("Otra123");
		vacio.setRol("Docente");
		vacio.setImagen_usuario("#FFFFFF");
		assertThat(vacio.getId_usuario()).isEqualTo(10);
		assertThat(vacio.getNombre()).isEqualTo("Luis");
		assertThat(vacio.getCorreo_electronico()).isEqualTo("luis@cbtis.mx");
		assertThat(vacio.getContrasena()).isEqualTo("Otra123");
		assertThat(vacio.getRol()).isEqualTo("Docente");
		assertThat(vacio.getImagen_usuario()).isEqualTo("#FFFFFF");
	}
}
