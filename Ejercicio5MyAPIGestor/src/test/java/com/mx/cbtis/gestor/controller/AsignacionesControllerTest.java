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

import com.mx.cbtis.gestor.dto.AsignacionRequest;
import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;

/**
 * Pruebas unitarias de AsignacionesController: casos de exito y de fracaso de cada endpoint.
 * El repositorio se sustituye por un mock de Mockito (no se usa BD).
 */
@ExtendWith(MockitoExtension.class)
class AsignacionesControllerTest {

	@Mock
	private AsignacionesRepository repository;

	private AsignacionesController controller;

	@BeforeEach
	void setUp() {
		controller = new AsignacionesController(repository);
	}

	private static Asignaciones muestra() {
		return new Asignaciones(5, 1, 7, "2026-11-05");
	}

	private static AsignacionRequest peticion() {
		return new AsignacionRequest(5, 1, 7, "2026-11-05");
	}

	private static AsignacionRequest peticionSinId() {
		return new AsignacionRequest(null, 1, 7, "2026-11-05");
	}

	@Test
	@DisplayName("Buscar por id: regresa el registro cuando existe")
	void buscarPorIdExistente() {
		Asignaciones esperado = muestra();
		when(repository.findById(5)).thenReturn(Optional.of(esperado));

		Asignaciones resultado = controller.getAsignacion(5);

		assertThat(resultado).isSameAs(esperado);
	}

	@Test
	@DisplayName("Buscar por id: regresa un objeto vacio cuando NO existe")
	void buscarPorIdInexistente() {
		when(repository.findById(99)).thenReturn(Optional.empty());

		Asignaciones resultado = controller.getAsignacion(99);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getIdAsignacion()).isNull();
	}

	@Test
	@DisplayName("Alta: convierte el DTO a entidad con TODOS sus campos, guarda y regresa 1")
	void altaExitosa() {
		ArgumentCaptor<Asignaciones> captor = ArgumentCaptor.forClass(Asignaciones.class);

		assertThat(controller.addAsignacion(peticion())).isEqualTo(RespuestaApi.EXITO);

		verify(repository).save(captor.capture());
		assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(muestra());
	}

	@Test
	@DisplayName("Alta: con cuerpo nulo NO guarda y regresa 0")
	void altaNula() {
		assertThat(controller.addAsignacion(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Actualizacion: convierte el DTO a entidad, guarda y regresa 1")
	void actualizacionExitosa() {
		ArgumentCaptor<Asignaciones> captor = ArgumentCaptor.forClass(Asignaciones.class);

		assertThat(controller.updateAsignacion(peticion())).isEqualTo(RespuestaApi.EXITO);

		verify(repository).save(captor.capture());
		assertThat(captor.getValue().getIdAsignacion()).isEqualTo(5);
	}

	@Test
	@DisplayName("Actualizacion: con cuerpo nulo NO guarda y regresa 0")
	void actualizacionNula() {
		assertThat(controller.updateAsignacion(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Baja: si el registro existe lo elimina por id y regresa 1")
	void bajaExitosa() {
		when(repository.existsById(5)).thenReturn(true);

		assertThat(controller.deleteAsignacion(peticion())).isEqualTo(RespuestaApi.EXITO);
		verify(repository).deleteById(5);
	}

	@Test
	@DisplayName("Baja: si el registro NO existe regresa 0 y no elimina")
	void bajaInexistente() {
		when(repository.existsById(5)).thenReturn(false);

		assertThat(controller.deleteAsignacion(peticion())).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).deleteById(any());
	}

	@Test
	@DisplayName("Baja: sin id regresa 0 sin consultar la BD")
	void bajaSinId() {
		assertThat(controller.deleteAsignacion(peticionSinId())).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).existsById(any());
	}

	@Test
	@DisplayName("Baja: con cuerpo nulo regresa 0")
	void bajaNula() {
		assertThat(controller.deleteAsignacion(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).deleteById(any());
	}

	@Test
	@DisplayName("Listado: regresa todos los registros en el mismo orden")
	void listadoConDatos() {
		Asignaciones primero = muestra();
		Asignaciones segundo = new Asignaciones();
		when(repository.findAll()).thenReturn(List.of(primero, segundo));

		assertThat(controller.getAsignaciones()).containsExactly(primero, segundo);
	}

	@Test
	@DisplayName("Listado: regresa lista vacia cuando no hay registros")
	void listadoVacio() {
		when(repository.findAll()).thenReturn(Collections.emptyList());

		assertThat(controller.getAsignaciones()).isEmpty();
	}
}
