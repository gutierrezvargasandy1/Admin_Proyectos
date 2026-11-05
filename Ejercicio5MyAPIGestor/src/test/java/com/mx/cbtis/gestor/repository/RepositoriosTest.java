package com.mx.cbtis.gestor.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.modelo.Usuario;

/**
 * Pruebas de base de datos (H2 en memoria): mapeo de entidades a tablas
 * y consultas personalizadas de los repositorios.
 * Cada prueba corre en una transaccion que se revierte al terminar.
 */
@DataJpaTest
class RepositoriosTest {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private TareasRepository tareasRepository;

	@Autowired
	private ComentariosRepository comentariosRepository;

	@Autowired
	private ProyectosRepository proyectosRepository;

	@Autowired
	private AsignacionesRepository asignacionesRepository;

	@Test
	@DisplayName("Usuario: busca por correo electronico")
	void usuarioPorCorreo() {
		usuarioRepository.save(new Usuario(null, "Ana", "ana@cbtis.mx", "$2a$hash", "Estudiante", "#70FAB7"));
		usuarioRepository.save(new Usuario(null, "Luis", "luis@cbtis.mx", "$2a$hash", "Docente", "#FFFFFF"));

		Optional<Usuario> encontrado = usuarioRepository.findFirstByCorreoElectronico("ana@cbtis.mx");

		assertThat(encontrado).isPresent();
		assertThat(encontrado.get().getNombre()).isEqualTo("Ana");
		assertThat(encontrado.get().getIdUsuario()).isNotNull();
	}

	@Test
	@DisplayName("Usuario: correo inexistente regresa Optional vacio")
	void usuarioCorreoInexistente() {
		usuarioRepository.save(new Usuario(null, "Ana", "ana@cbtis.mx", "$2a$hash", "Estudiante", "#70FAB7"));

		assertThat(usuarioRepository.findFirstByCorreoElectronico("nadie@cbtis.mx")).isEmpty();
	}

	@Test
	@DisplayName("Tareas: filtra solo las tareas del proyecto indicado")
	void tareasPorProyecto() {
		tareasRepository.save(new Tareas(null, "BD", "Tablas", "2026-06-13", "pendiente", "danger", 1));
		tareasRepository.save(new Tareas(null, "API", "Endpoints", "2026-06-20", "proceso", "warning", 1));
		tareasRepository.save(new Tareas(null, "Front", "Vistas", "2026-06-27", "pendiente", "info", 2));

		List<Tareas> delProyecto1 = tareasRepository.findByIdProyecto(1);

		assertThat(delProyecto1).hasSize(2)
				.extracting(Tareas::getNombre).containsExactlyInAnyOrder("BD", "API");
		assertThat(tareasRepository.findByIdProyecto(3)).isEmpty();
	}

	@Test
	@DisplayName("Comentarios: filtra solo los comentarios de la tarea indicada")
	void comentariosPorTarea() {
		comentariosRepository.save(new Comentarios(null, "Ana", 10, "Primero", "2026-11-05 10:00"));
		comentariosRepository.save(new Comentarios(null, "Luis", 10, "Segundo", "2026-11-05 11:00"));
		comentariosRepository.save(new Comentarios(null, "Ana", 20, "Otra tarea", "2026-11-05 12:00"));

		List<Comentarios> deLaTarea10 = comentariosRepository.findByIdTarea(10);

		assertThat(deLaTarea10).hasSize(2)
				.extracting(Comentarios::getTextoComentario).containsExactlyInAnyOrder("Primero", "Segundo");
		assertThat(comentariosRepository.findByIdTarea(99)).isEmpty();
	}

	@Test
	@DisplayName("Proyectos: alta, consulta, actualizacion y baja (CRUD completo)")
	void proyectosCrud() {
		Proyectos guardado = proyectosRepository.save(
				new Proyectos(null, "My API Rest", "API", "2026-01-15", "2026-06-30", "En proceso", "/fondo.png"));
		Integer id = guardado.getIdProyecto();
		assertThat(id).isNotNull();

		guardado.setEstadoProyecto("Terminado");
		proyectosRepository.save(guardado);
		assertThat(proyectosRepository.findById(id).orElseThrow().getEstadoProyecto()).isEqualTo("Terminado");

		proyectosRepository.delete(guardado);
		assertThat(proyectosRepository.findById(id)).isEmpty();
	}

	@Test
	@DisplayName("Asignaciones: alta y listado")
	void asignacionesAltaYListado() {
		asignacionesRepository.save(new Asignaciones(null, 1, 10, "2026-11-05"));
		asignacionesRepository.save(new Asignaciones(null, 2, 10, "2026-11-06"));

		assertThat(asignacionesRepository.findAll()).hasSize(2);
	}
}
