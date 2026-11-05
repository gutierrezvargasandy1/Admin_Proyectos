package com.mx.cbtis.gestor.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;

/**
 * Pruebas unitarias de ComentariosController: casos de exito y de fracaso de cada endpoint.
 * El repositorio se sustituye por un mock de Mockito (no se usa BD).
 */
@ExtendWith(MockitoExtension.class)
class ComentariosControllerTest {

	@Mock
	private ComentariosRepository repository;

	private ComentariosController controller;

	@BeforeEach
	void setUp() {
		controller = new ComentariosController(repository);
	}

	private static Comentarios muestra() {
		return new Comentarios(5, "Ana Lopez", 7, "Revisar el avance", "2026-11-05 10:00");
	}

	@Test
	@DisplayName("Buscar por id: regresa el registro cuando existe")
	void buscarPorIdExistente() {
		Comentarios esperado = muestra();
		when(repository.findById(5)).thenReturn(Optional.of(esperado));

		Comentarios resultado = controller.getComentario(5);

		assertThat(resultado).isSameAs(esperado);
		assertThat(resultado.getIdComentario()).isEqualTo(5);
	}

	@Test
	@DisplayName("Buscar por id: regresa un objeto vacio cuando NO existe")
	void buscarPorIdInexistente() {
		when(repository.findById(99)).thenReturn(Optional.empty());

		Comentarios resultado = controller.getComentario(99);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getIdComentario()).isNull();
	}

	@Test
	@DisplayName("Alta: guarda el registro y regresa 1")
	void altaExitosa() {
		Comentarios nuevo = muestra();

		assertThat(controller.addComentario(nuevo)).isEqualTo(RespuestaApi.EXITO);
		verify(repository).save(nuevo);
	}

	@Test
	@DisplayName("Alta: con cuerpo nulo NO guarda y regresa 0")
	void altaNula() {
		assertThat(controller.addComentario(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Actualizacion: guarda el registro y regresa 1")
	void actualizacionExitosa() {
		Comentarios existente = muestra();

		assertThat(controller.updateComentario(existente)).isEqualTo(RespuestaApi.EXITO);
		verify(repository).save(existente);
	}

	@Test
	@DisplayName("Actualizacion: con cuerpo nulo NO guarda y regresa 0")
	void actualizacionNula() {
		assertThat(controller.updateComentario(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Baja: elimina el registro y regresa 1")
	void bajaExitosa() {
		Comentarios existente = muestra();

		assertThat(controller.deleteComentario(existente)).isEqualTo(RespuestaApi.EXITO);
		verify(repository).delete(existente);
	}

	@Test
	@DisplayName("Baja: con cuerpo nulo NO elimina y regresa 0")
	void bajaNula() {
		assertThat(controller.deleteComentario(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).delete(any());
	}

	@Test
	@DisplayName("Listado: regresa todos los registros en el mismo orden")
	void listadoConDatos() {
		Comentarios primero = muestra();
		Comentarios segundo = new Comentarios();
		when(repository.findAll()).thenReturn(List.of(primero, segundo));

		List<Comentarios> resultado = controller.getComentarios();

		assertThat(resultado).containsExactly(primero, segundo);
	}

	@Test
	@DisplayName("Listado: regresa lista vacia cuando no hay registros")
	void listadoVacio() {
		when(repository.findAll()).thenReturn(Collections.emptyList());

		assertThat(controller.getComentarios()).isEmpty();
	}

	@Test
	@DisplayName("Filtro por tarea: regresa los registros relacionados")
	void filtroConDatos() {
		Comentarios filtro = new Comentarios();
		filtro.setIdTarea(7);
		List<Comentarios> esperados = List.of(muestra());
		when(repository.findByIdTarea(7)).thenReturn(esperados);

		assertThat(controller.getComentariosPorTarea(filtro)).isEqualTo(esperados);
	}

	@Test
	@DisplayName("Filtro por tarea: sin tarea regresa lista vacia y no consulta la BD")
	void filtroSinLlave() {
		assertThat(controller.getComentariosPorTarea(new Comentarios())).isEmpty();
		verify(repository, never()).findByIdTarea(any());
	}

	@Test
	@DisplayName("Filtro por tarea: con cuerpo nulo regresa lista vacia")
	void filtroNulo() {
		assertThat(controller.getComentariosPorTarea(null)).isEmpty();
		verify(repository, never()).findByIdTarea(any());
	}
}
