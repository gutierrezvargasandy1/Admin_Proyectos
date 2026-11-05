package com.mx.cbtis.gestor.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.mx.cbtis.gestor.config.SeguridadConfig;
import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.ProyectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

/**
 * Pruebas de la capa HTTP: rutas, metodos, parametros, codigos de estado
 * y formato JSON que consume el frontend. Los repositorios son mocks.
 */
@WebMvcTest
@Import(SeguridadConfig.class)
class ApiWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockBean
	private UsuarioRepository usuarioRepository;

	@MockBean
	private ProyectosRepository proyectosRepository;

	@MockBean
	private TareasRepository tareasRepository;

	@MockBean
	private ComentariosRepository comentariosRepository;

	@MockBean
	private AsignacionesRepository asignacionesRepository;

	@Test
	@DisplayName("GET /buscarProyecto?id_proyecto=197 regresa el proyecto en JSON snake_case")
	void buscarProyecto() throws Exception {
		when(proyectosRepository.findById(197)).thenReturn(Optional.of(
				new Proyectos(197, "My API Rest", "API", "2026-05-31", "2026-06-29", "En proceso", "/Images/a.gif")));

		mockMvc.perform(get("/buscarProyecto").param("id_proyecto", "197"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id_proyecto").value(197))
				.andExpect(jsonPath("$.nombre").value("My API Rest"))
				.andExpect(jsonPath("$.estado_proyecto").value("En proceso"));
	}

	@Test
	@DisplayName("GET /buscarProyecto sin parametro regresa 400")
	void buscarProyectoSinParametro() throws Exception {
		mockMvc.perform(get("/buscarProyecto"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /addProyecto con JSON valido regresa 1")
	void altaProyecto() throws Exception {
		mockMvc.perform(post("/addProyecto")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Nuevo\",\"descripcion\":\"Desc\",\"estado_proyecto\":\"En proceso\"}"))
				.andExpect(status().isOk())
				.andExpect(content().string("1"));

		verify(proyectosRepository).save(any(Proyectos.class));
	}

	@Test
	@DisplayName("POST /addProyecto sin cuerpo regresa 400 y no guarda")
	void altaProyectoSinCuerpo() throws Exception {
		mockMvc.perform(post("/addProyecto").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

		verify(proyectosRepository, never()).save(any());
	}

	@Test
	@DisplayName("POST /getTarea con id_proyecto regresa las tareas del proyecto")
	void tareasPorProyecto() throws Exception {
		when(tareasRepository.findByIdProyecto(197)).thenReturn(List.of(
				new Tareas(199, "Base de datos", "Tablas", "2026-06-13", "terminado", "danger", 197)));

		mockMvc.perform(post("/getTarea")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_proyecto\":197}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id_tarea").value(199))
				.andExpect(jsonPath("$[0].id_proyecto").value(197));
	}

	@Test
	@DisplayName("POST /getComentario con id_tarea regresa los comentarios de la tarea")
	void comentariosPorTarea() throws Exception {
		when(comentariosRepository.findByIdTarea(199)).thenReturn(List.of(
				new Comentarios(201, "Ana", 199, "Subirlo YA", "2026-06-26 13:48")));

		mockMvc.perform(post("/getComentario")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_tarea\":199}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].texto_comentario").value("Subirlo YA"));
	}

	@Test
	@DisplayName("DELETE /deleteComentario de un comentario existente regresa 1")
	void bajaComentario() throws Exception {
		when(comentariosRepository.existsById(201)).thenReturn(true);

		mockMvc.perform(delete("/deleteComentario")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_comentario\":201,\"texto_comentario\":\"Subirlo YA\"}"))
				.andExpect(status().isOk())
				.andExpect(content().string("1"));

		verify(comentariosRepository).deleteById(201);
	}

	@Test
	@DisplayName("POST /addTareas ignora campos que no pertenecen al DTO (proteccion mass assignment)")
	void altaTareaIgnoraCamposExtra() throws Exception {
		mockMvc.perform(post("/addTareas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"BD\",\"id_proyecto\":197,\"campoInventado\":\"x\"}"))
				.andExpect(status().isOk())
				.andExpect(content().string("1"));

		verify(tareasRepository).save(any(Tareas.class));
	}

	@Test
	@DisplayName("POST /buscarAsignaciones regresa el listado")
	void listadoAsignaciones() throws Exception {
		when(asignacionesRepository.findAll()).thenReturn(List.of(new Asignaciones(1, 2, 3, "2026-11-05")));

		mockMvc.perform(post("/buscarAsignaciones"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id_asignacion").value(1))
				.andExpect(jsonPath("$[0].fecha_asignacion").value("2026-11-05"));
	}

	@Test
	@DisplayName("POST /getUsuarioLogin correcto regresa al usuario SIN la contrasena")
	void loginCorrectoNoExponeContrasena() throws Exception {
		when(usuarioRepository.findFirstByCorreoElectronico("ana@cbtis.mx")).thenReturn(Optional.of(
				new Usuario(131, "Ana", "ana@cbtis.mx", passwordEncoder.encode("Secreta123"), "Estudiante", "#70FAB7")));

		mockMvc.perform(post("/getUsuarioLogin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"ana@cbtis.mx\",\"contrasena\":\"Secreta123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id_usuario").value(131))
				.andExpect(jsonPath("$.nombre").value("Ana"))
				.andExpect(jsonPath("$.contrasena").doesNotExist());
	}

	@Test
	@DisplayName("POST /getUsuarioLogin incorrecto regresa nombre '000'")
	void loginIncorrecto() throws Exception {
		when(usuarioRepository.findFirstByCorreoElectronico("ana@cbtis.mx")).thenReturn(Optional.empty());

		mockMvc.perform(post("/getUsuarioLogin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"ana@cbtis.mx\",\"contrasena\":\"mala\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("000"));
	}

	@Test
	@DisplayName("POST /buscarUsuarios nunca expone contrasenas")
	void listadoUsuariosSinContrasenas() throws Exception {
		when(usuarioRepository.findAll()).thenReturn(List.of(
				new Usuario(1, "Ana", "ana@cbtis.mx", "$2a$10$hash", "Estudiante", "#70FAB7")));

		mockMvc.perform(post("/buscarUsuarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].correo_electronico").value("ana@cbtis.mx"))
				.andExpect(jsonPath("$[0].contrasena").doesNotExist());
	}
}
