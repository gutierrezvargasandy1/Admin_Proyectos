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

	@Test
	@DisplayName("Buscar por id: regresa el registro cuando existe")
	void buscarPorIdExistente() {
		Proyectos esperado = muestra();
		when(repository.findById(5)).thenReturn(Optional.of(esperado));

		Proyectos resultado = controller.getProyecto(5);

		assertThat(resultado).isSameAs(esperado);
		assertThat(resultado.getIdProyecto()).isEqualTo(5);
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
	@DisplayName("Alta: guarda el registro y regresa 1")
	void altaExitosa() {
		Proyectos nuevo = muestra();

		assertThat(controller.addProyecto(nuevo)).isEqualTo(RespuestaApi.EXITO);
		verify(repository).save(nuevo);
	}

	@Test
	@DisplayName("Alta: con cuerpo nulo NO guarda y regresa 0")
	void altaNula() {
		assertThat(controller.addProyecto(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Actualizacion: guarda el registro y regresa 1")
	void actualizacionExitosa() {
		Proyectos existente = muestra();

		assertThat(controller.updateProyecto(existente)).isEqualTo(RespuestaApi.EXITO);
		verify(repository).save(existente);
	}

	@Test
	@DisplayName("Actualizacion: con cuerpo nulo NO guarda y regresa 0")
	void actualizacionNula() {
		assertThat(controller.updateProyecto(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Baja: elimina el registro y regresa 1")
	void bajaExitosa() {
		Proyectos existente = muestra();

		assertThat(controller.deleteProyecto(existente)).isEqualTo(RespuestaApi.EXITO);
		verify(repository).delete(existente);
	}

	@Test
	@DisplayName("Baja: con cuerpo nulo NO elimina y regresa 0")
	void bajaNula() {
		assertThat(controller.deleteProyecto(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).delete(any());
	}

	@Test
	@DisplayName("Listado: regresa todos los registros en el mismo orden")
	void listadoConDatos() {
		Proyectos primero = muestra();
		Proyectos segundo = new Proyectos();
		when(repository.findAll()).thenReturn(List.of(primero, segundo));

		List<Proyectos> resultado = controller.getProyectos();

		assertThat(resultado).containsExactly(primero, segundo);
	}

	@Test
	@DisplayName("Listado: regresa lista vacia cuando no hay registros")
	void listadoVacio() {
		when(repository.findAll()).thenReturn(Collections.emptyList());

		assertThat(controller.getProyectos()).isEmpty();
	}
}
