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

import com.mx.cbtis.gestor.dto.ProyectoRequest;
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProyectosRepository;

/**
 * Pruebas unitarias de ProyectosController: casos de exito y de fracaso de cada endpoint.
 * El repositorio se sustituye por un mock de Mockito (no se usa BD).
 */
@ExtendWith(MockitoExtension.class)
class ProyectosControllerTest {

	@Mock
	private ProyectosRepository repository;

	private ProyectosController controller;

	@BeforeEach
	void setUp() {
		controller = new ProyectosController(repository);
	}

	private static Proyectos muestra() {
		return new Proyectos(5, "My API Rest", "API con 5 tablas", "2026-01-15", "2026-06-30", "En proceso", "/Images/animacion1.gif");
	}

	private static ProyectoRequest peticion() {
		return new ProyectoRequest(5, "My API Rest", "API con 5 tablas", "2026-01-15", "2026-06-30", "En proceso", "/Images/animacion1.gif");
	}

	private static ProyectoRequest peticionSinId() {
		return new ProyectoRequest(null, "My API Rest", "API con 5 tablas", "2026-01-15", "2026-06-30", "En proceso", "/Images/animacion1.gif");
	}

	@Test
	@DisplayName("Buscar por id: regresa el registro cuando existe")
	void buscarPorIdExistente() {
		Proyectos esperado = muestra();
		when(repository.findById(5)).thenReturn(Optional.of(esperado));

		Proyectos resultado = controller.getProyecto(5);

		assertThat(resultado).isSameAs(esperado);
	}

	@Test
	@DisplayName("Buscar por id: regresa un objeto vacio cuando NO existe")
	void buscarPorIdInexistente() {
		when(repository.findById(99)).thenReturn(Optional.empty());

		Proyectos resultado = controller.getProyecto(99);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getIdProyecto()).isNull();
	}

	@Test
	@DisplayName("Alta: convierte el DTO a entidad con TODOS sus campos, guarda y regresa 1")
	void altaExitosa() {
		ArgumentCaptor<Proyectos> captor = ArgumentCaptor.forClass(Proyectos.class);

		assertThat(controller.addProyecto(peticion())).isEqualTo(RespuestaApi.EXITO);

		verify(repository).save(captor.capture());
		assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(muestra());
	}

	@Test
	@DisplayName("Alta: con cuerpo nulo NO guarda y regresa 0")
	void altaNula() {
		assertThat(controller.addProyecto(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Actualizacion: convierte el DTO a entidad, guarda y regresa 1")
	void actualizacionExitosa() {
		ArgumentCaptor<Proyectos> captor = ArgumentCaptor.forClass(Proyectos.class);

		assertThat(controller.updateProyecto(peticion())).isEqualTo(RespuestaApi.EXITO);

		verify(repository).save(captor.capture());
		assertThat(captor.getValue().getIdProyecto()).isEqualTo(5);
	}

	@Test
	@DisplayName("Actualizacion: con cuerpo nulo NO guarda y regresa 0")
	void actualizacionNula() {
		assertThat(controller.updateProyecto(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Baja: si el registro existe lo elimina por id y regresa 1")
	void bajaExitosa() {
		when(repository.existsById(5)).thenReturn(true);

		assertThat(controller.deleteProyecto(peticion())).isEqualTo(RespuestaApi.EXITO);
		verify(repository).deleteById(5);
	}

	@Test
	@DisplayName("Baja: si el registro NO existe regresa 0 y no elimina")
	void bajaInexistente() {
		when(repository.existsById(5)).thenReturn(false);

		assertThat(controller.deleteProyecto(peticion())).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).deleteById(any());
	}

	@Test
	@DisplayName("Baja: sin id regresa 0 sin consultar la BD")
	void bajaSinId() {
		assertThat(controller.deleteProyecto(peticionSinId())).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).existsById(any());
	}

	@Test
	@DisplayName("Baja: con cuerpo nulo regresa 0")
	void bajaNula() {
		assertThat(controller.deleteProyecto(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).deleteById(any());
	}

	@Test
	@DisplayName("Listado: regresa todos los registros en el mismo orden")
	void listadoConDatos() {
		Proyectos primero = muestra();
		Proyectos segundo = new Proyectos();
		when(repository.findAll()).thenReturn(List.of(primero, segundo));

		assertThat(controller.getProyectos()).containsExactly(primero, segundo);
	}

	@Test
	@DisplayName("Listado: regresa lista vacia cuando no hay registros")
	void listadoVacio() {
		when(repository.findAll()).thenReturn(Collections.emptyList());

		assertThat(controller.getProyectos()).isEmpty();
	}
}
