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

import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.TareasRepository;

/**
 * Pruebas unitarias de TareasController: casos de exito y de fracaso de cada endpoint.
 * El repositorio se sustituye por un mock de Mockito (no se usa BD).
 */
@ExtendWith(MockitoExtension.class)
class TareasControllerTest {

	@Mock
	private TareasRepository repository;

	private TareasController controller;

	@BeforeEach
	void setUp() {
		controller = new TareasController(repository);
	}

	private static Tareas muestra() {
		return new Tareas(5, "Base de datos", "Estructura de tablas", "2026-06-13", "pendiente", "danger", 7);
	}

	@Test
	@DisplayName("Buscar por id: regresa el registro cuando existe")
	void buscarPorIdExistente() {
		Tareas esperado = muestra();
		when(repository.findById(5)).thenReturn(Optional.of(esperado));

		Tareas resultado = controller.getTarea(5);

		assertThat(resultado).isSameAs(esperado);
		assertThat(resultado.getIdTarea()).isEqualTo(5);
	}

	@Test
	@DisplayName("Buscar por id: regresa un objeto vacio cuando NO existe")
	void buscarPorIdInexistente() {
		when(repository.findById(99)).thenReturn(Optional.empty());

		Tareas resultado = controller.getTarea(99);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getIdTarea()).isNull();
	}

	@Test
	@DisplayName("Alta: guarda el registro y regresa 1")
	void altaExitosa() {
		Tareas nuevo = muestra();

		assertThat(controller.addTareas(nuevo)).isEqualTo(RespuestaApi.EXITO);
		verify(repository).save(nuevo);
	}

	@Test
	@DisplayName("Alta: con cuerpo nulo NO guarda y regresa 0")
	void altaNula() {
		assertThat(controller.addTareas(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Actualizacion: guarda el registro y regresa 1")
	void actualizacionExitosa() {
		Tareas existente = muestra();

		assertThat(controller.updateTareas(existente)).isEqualTo(RespuestaApi.EXITO);
		verify(repository).save(existente);
	}

	@Test
	@DisplayName("Actualizacion: con cuerpo nulo NO guarda y regresa 0")
	void actualizacionNula() {
		assertThat(controller.updateTareas(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Baja: elimina el registro y regresa 1")
	void bajaExitosa() {
		Tareas existente = muestra();

		assertThat(controller.deleteTareas(existente)).isEqualTo(RespuestaApi.EXITO);
		verify(repository).delete(existente);
	}

	@Test
	@DisplayName("Baja: con cuerpo nulo NO elimina y regresa 0")
	void bajaNula() {
		assertThat(controller.deleteTareas(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).delete(any());
	}

	@Test
	@DisplayName("Listado: regresa todos los registros en el mismo orden")
	void listadoConDatos() {
		Tareas primero = muestra();
		Tareas segundo = new Tareas();
		when(repository.findAll()).thenReturn(List.of(primero, segundo));

		List<Tareas> resultado = controller.getTareas();

		assertThat(resultado).containsExactly(primero, segundo);
	}

	@Test
	@DisplayName("Listado: regresa lista vacia cuando no hay registros")
	void listadoVacio() {
		when(repository.findAll()).thenReturn(Collections.emptyList());

		assertThat(controller.getTareas()).isEmpty();
	}

	@Test
	@DisplayName("Filtro por proyecto: regresa los registros relacionados")
	void filtroConDatos() {
		Tareas filtro = new Tareas();
		filtro.setIdProyecto(7);
		List<Tareas> esperados = List.of(muestra());
		when(repository.findByIdProyecto(7)).thenReturn(esperados);

		assertThat(controller.getTareasPorProyecto(filtro)).isEqualTo(esperados);
	}

	@Test
	@DisplayName("Filtro por proyecto: sin proyecto regresa lista vacia y no consulta la BD")
	void filtroSinLlave() {
		assertThat(controller.getTareasPorProyecto(new Tareas())).isEmpty();
		verify(repository, never()).findByIdProyecto(any());
	}

	@Test
	@DisplayName("Filtro por proyecto: con cuerpo nulo regresa lista vacia")
	void filtroNulo() {
		assertThat(controller.getTareasPorProyecto(null)).isEmpty();
		verify(repository, never()).findByIdProyecto(any());
	}
}
