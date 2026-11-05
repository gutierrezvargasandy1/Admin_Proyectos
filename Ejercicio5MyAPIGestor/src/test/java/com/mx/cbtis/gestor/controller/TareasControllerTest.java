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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mx.cbtis.gestor.dto.FiltroProyectoRequest;
import com.mx.cbtis.gestor.dto.TareaRequest;
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

	private static TareaRequest peticion() {
		return new TareaRequest(5, "Base de datos", "Estructura de tablas", "2026-06-13", "pendiente", "danger", 7);
	}

	private static TareaRequest peticionSinId() {
		return new TareaRequest(null, "Base de datos", "Estructura de tablas", "2026-06-13", "pendiente", "danger", 7);
	}

	@Test
	@DisplayName("Buscar por id: regresa el registro cuando existe")
	void buscarPorIdExistente() {
		Tareas esperado = muestra();
		when(repository.findById(5)).thenReturn(Optional.of(esperado));

		Tareas resultado = controller.getTarea(5);

		assertThat(resultado).isSameAs(esperado);
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
	@DisplayName("Alta: convierte el DTO a entidad con TODOS sus campos, guarda y regresa 1")
	void altaExitosa() {
		ArgumentCaptor<Tareas> captor = ArgumentCaptor.forClass(Tareas.class);

		assertThat(controller.addTareas(peticion())).isEqualTo(RespuestaApi.EXITO);

		verify(repository).save(captor.capture());
		assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(muestra());
	}

	@Test
	@DisplayName("Alta: con cuerpo nulo NO guarda y regresa 0")
	void altaNula() {
		assertThat(controller.addTareas(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Actualizacion: convierte el DTO a entidad, guarda y regresa 1")
	void actualizacionExitosa() {
		ArgumentCaptor<Tareas> captor = ArgumentCaptor.forClass(Tareas.class);

		assertThat(controller.updateTareas(peticion())).isEqualTo(RespuestaApi.EXITO);

		verify(repository).save(captor.capture());
		assertThat(captor.getValue().getIdTarea()).isEqualTo(5);
	}

	@Test
	@DisplayName("Actualizacion: con cuerpo nulo NO guarda y regresa 0")
	void actualizacionNula() {
		assertThat(controller.updateTareas(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Baja: si el registro existe lo elimina por id y regresa 1")
	void bajaExitosa() {
		when(repository.existsById(5)).thenReturn(true);

		assertThat(controller.deleteTareas(peticion())).isEqualTo(RespuestaApi.EXITO);
		verify(repository).deleteById(5);
	}

	@Test
	@DisplayName("Baja: si el registro NO existe regresa 0 y no elimina")
	void bajaInexistente() {
		when(repository.existsById(5)).thenReturn(false);

		assertThat(controller.deleteTareas(peticion())).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).deleteById(any());
	}

	@Test
	@DisplayName("Baja: sin id regresa 0 sin consultar la BD")
	void bajaSinId() {
		assertThat(controller.deleteTareas(peticionSinId())).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).existsById(any());
	}

	@Test
	@DisplayName("Baja: con cuerpo nulo regresa 0")
	void bajaNula() {
		assertThat(controller.deleteTareas(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).deleteById(any());
	}

	@Test
	@DisplayName("Listado: regresa todos los registros en el mismo orden")
	void listadoConDatos() {
		Tareas primero = muestra();
		Tareas segundo = new Tareas();
		when(repository.findAll()).thenReturn(List.of(primero, segundo));

		assertThat(controller.getTareas()).containsExactly(primero, segundo);
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
		List<Tareas> esperados = List.of(muestra());
		when(repository.findByIdProyecto(7)).thenReturn(esperados);

		assertThat(controller.getTareasPorProyecto(new FiltroProyectoRequest(7))).isEqualTo(esperados);
	}

	@Test
	@DisplayName("Filtro por proyecto: sin proyecto regresa lista vacia y no consulta la BD")
	void filtroSinLlave() {
		assertThat(controller.getTareasPorProyecto(new FiltroProyectoRequest(null))).isEmpty();
		verify(repository, never()).findByIdProyecto(any());
	}

	@Test
	@DisplayName("Filtro por proyecto: con cuerpo nulo regresa lista vacia")
	void filtroNulo() {
		assertThat(controller.getTareasPorProyecto(null)).isEmpty();
		verify(repository, never()).findByIdProyecto(any());
	}
}
