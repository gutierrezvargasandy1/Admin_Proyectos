package com.mx.cbtis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.mx.cbtis.cliente.GestorApiClient;
import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

/**
 * Pruebas del controlador del frontend: vistas, modelo, redirecciones y respuestas JSON.
 * La API se sustituye por un mock de GestorApiClient.
 */
class GestorControllerTest {

	private GestorApiClient api;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		api = mock(GestorApiClient.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new GestorController(api))
				.setViewResolvers(new InternalResourceViewResolver("/templates/", ".html"))
				.build();
	}

	// ------------------------------------------------------------ Vistas

	@Test
	@DisplayName("Vistas estaticas: home, tutorial, manual y login")
	void vistasEstaticas() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
		mockMvc.perform(get("/tutorial")).andExpect(status().isOk()).andExpect(view().name("tutorial"));
		mockMvc.perform(get("/manual")).andExpect(status().isOk()).andExpect(view().name("manual"));
		mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
	}

	@Test
	@DisplayName("GET /register: muestra el formulario con un usuario vacio")
	void register() throws Exception {
		mockMvc.perform(get("/register"))
				.andExpect(status().isOk())
				.andExpect(view().name("register"))
				.andExpect(model().attributeExists("usuario"));
	}

	@Test
	@DisplayName("GET /inicio: muestra el tablero con un proyecto vacio para el formulario")
	void inicio() throws Exception {
		mockMvc.perform(get("/inicio"))
				.andExpect(status().isOk())
				.andExpect(view().name("inicio"))
				.andExpect(model().attributeExists("proyectos"));
	}

	@Test
	@DisplayName("GET /proyecto: carga la vista con la lista de usuarios de la API")
	void proyecto() throws Exception {
		List<Usuario> usuarios = List.of(new Usuario(1, "Ana", "ana@cbtis.mx", null, "Estudiante", "#70FAB7"));
		when(api.buscarUsuarios()).thenReturn(usuarios);

		mockMvc.perform(get("/proyecto"))
				.andExpect(status().isOk())
				.andExpect(view().name(GestorController.VISTA_PROYECTO))
				.andExpect(model().attributeExists("tarea", "comentarios"))
				.andExpect(model().attribute("listaUsuarios", usuarios));
	}

	// ------------------------------------------------------------ Usuarios

	@Test
	@DisplayName("POST /saveUsuario: envia el formulario a la API y redirige al login")
	void saveUsuario() throws Exception {
		mockMvc.perform(post("/saveUsuario")
				.param("nombre", "Ana")
				.param("correo_electronico", "ana@cbtis.mx")
				.param("contrasena", "Secreta123"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));

		verify(api).addUsuario(argThat(u -> "ana@cbtis.mx".equals(u.getCorreo_electronico())
				&& "Secreta123".equals(u.getContrasena())));
	}

	@Test
	@DisplayName("POST /verificarLogin: regresa el usuario que responde la API")
	void verificarLogin() throws Exception {
		when(api.login(any(Usuario.class))).thenReturn(new Usuario(131, "Ana", "ana@cbtis.mx", null, "Estudiante", "#70FAB7"));

		mockMvc.perform(post("/verificarLogin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"ana@cbtis.mx\",\"contrasena\":\"Secreta123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Ana"))
				.andExpect(jsonPath("$.id_usuario").value(131));
	}

	@Test
	@DisplayName("POST /verificarLogin: credenciales invalidas regresan nombre '000'")
	void verificarLoginInvalido() throws Exception {
		Usuario invalido = new Usuario();
		invalido.setNombre("000");
		when(api.login(any(Usuario.class))).thenReturn(invalido);

		mockMvc.perform(post("/verificarLogin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"ana@cbtis.mx\",\"contrasena\":\"mala\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("000"));
	}

	@Test
	@DisplayName("POST /consultarUsuarios: regresa la lista de usuarios en JSON")
	void consultarUsuarios() throws Exception {
		when(api.buscarUsuarios()).thenReturn(List.of(new Usuario(1, "Ana", null, null, null, null)));

		mockMvc.perform(post("/consultarUsuarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Ana"));
	}

	// ------------------------------------------------------------ Proyectos

	@Test
	@DisplayName("POST /saveProyecto: envia el formulario a la API y regresa a inicio")
	void saveProyecto() throws Exception {
		mockMvc.perform(post("/saveProyecto")
				.param("nombre", "My API Rest")
				.param("estado_proyecto", "En proceso"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/inicio"));

		verify(api).addProyecto(argThat(p -> "My API Rest".equals(p.getNombre())));
	}

	@Test
	@DisplayName("POST /consultarProyectos: regresa los proyectos en JSON")
	void consultarProyectos() throws Exception {
		when(api.buscarProyectos()).thenReturn(List.of(
				new Proyectos(197, "My API Rest", "API", "2026-05-31", "2026-06-29", "En proceso", "/a.gif")));

		mockMvc.perform(post("/consultarProyectos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id_proyecto").value(197))
				.andExpect(jsonPath("$[0].estado_proyecto").value("En proceso"));
	}

	@Test
	@DisplayName("POST /actualizarProyecto: consulta el proyecto por id para editarlo")
	void actualizarProyecto() throws Exception {
		when(api.buscarProyecto(197)).thenReturn(
				new Proyectos(197, "My API Rest", "API", "2026-05-31", "2026-06-29", "En proceso", "/a.gif"));

		mockMvc.perform(post("/actualizarProyecto")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_proyecto\":197}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("My API Rest"));
	}

	@Test
	@DisplayName("POST /eliminarProyecto: pide a la API eliminar y regresa a inicio")
	void eliminarProyecto() throws Exception {
		mockMvc.perform(post("/eliminarProyecto")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_proyecto\":197}"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/inicio"));

		verify(api).deleteProyecto(argThat(p -> Integer.valueOf(197).equals(p.getId_proyecto())));
	}

	// ------------------------------------------------------------ Tareas

	@Test
	@DisplayName("POST /saveTarea: guarda y redirige al proyecto conservando id_proyecto")
	void saveTarea() throws Exception {
		mockMvc.perform(post("/saveTarea")
				.param("nombre", "BD")
				.param("id_proyecto", "197"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/proyecto?id_proyecto=197"));

		verify(api).addTarea(argThat(t -> "BD".equals(t.getNombre())));
	}

	@Test
	@DisplayName("POST /verificarTarea y /cambiosTareas: regresan el codigo de la API")
	void verificarYCambiosTarea() throws Exception {
		when(api.addTarea(any(Tareas.class))).thenReturn(1);

		mockMvc.perform(post("/verificarTarea")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"BD\",\"lista\":\"pendiente\",\"id_proyecto\":197}"))
				.andExpect(status().isOk())
				.andExpect(content().string("1"));

		mockMvc.perform(post("/cambiosTareas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_tarea\":199,\"lista\":\"terminado\"}"))
				.andExpect(status().isOk())
				.andExpect(content().string("1"));
	}

	@Test
	@DisplayName("POST /obtenerTareas: regresa las tareas del proyecto")
	void obtenerTareas() throws Exception {
		when(api.tareasPorProyecto(any(Tareas.class))).thenReturn(List.of(
				new Tareas(199, "BD", "Tablas", "2026-06-13", "terminado", "danger", 197)));

		mockMvc.perform(post("/obtenerTareas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_proyecto\":197}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id_tarea").value(199));
	}

	@Test
	@DisplayName("POST /consultarTareas: regresa todas las tareas")
	void consultarTareas() throws Exception {
		when(api.buscarTareas()).thenReturn(List.of(new Tareas(199, "BD", null, null, null, null, 197)));

		mockMvc.perform(post("/consultarTareas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("BD"));
	}

	@Test
	@DisplayName("POST /actualizarTarea: consulta la tarea por id para editarla")
	void actualizarTarea() throws Exception {
		when(api.buscarTarea(199)).thenReturn(new Tareas(199, "BD", "Tablas", "2026-06-13", "terminado", "danger", 197));

		mockMvc.perform(post("/actualizarTarea")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_tarea\":199}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.descripcion").value("Tablas"));
	}

	@Test
	@DisplayName("POST /eliminarTarea: pide a la API eliminar y regresa al proyecto")
	void eliminarTarea() throws Exception {
		mockMvc.perform(post("/eliminarTarea")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_tarea\":199}"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/proyecto"));

		verify(api).deleteTarea(argThat(t -> Integer.valueOf(199).equals(t.getId_tarea())));
	}

	// ------------------------------------------------------------ Comentarios

	@Test
	@DisplayName("POST /saveComentario: regresa el codigo de la API")
	void saveComentario() throws Exception {
		when(api.addComentario(any(Comentarios.class))).thenReturn(1);

		mockMvc.perform(post("/saveComentario")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"texto_comentario\":\"Subirlo YA\",\"id_tarea\":199}"))
				.andExpect(status().isOk())
				.andExpect(content().string("1"));
	}

	@Test
	@DisplayName("POST /obtenerComentarios: regresa los comentarios de la tarea")
	void obtenerComentarios() throws Exception {
		when(api.comentariosPorTarea(any(Comentarios.class))).thenReturn(List.of(
				new Comentarios(201, "Ana", 199, "Subirlo YA", "2026-06-26 13:48")));

		mockMvc.perform(post("/obtenerComentarios")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_tarea\":199}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].texto_comentario").value("Subirlo YA"));
	}

	@Test
	@DisplayName("POST /consultarComentarios: regresa todos los comentarios")
	void consultarComentarios() throws Exception {
		when(api.buscarComentarios()).thenReturn(List.of(new Comentarios(201, "Ana", 199, "Hola", null)));

		mockMvc.perform(post("/consultarComentarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id_comentario").value(201));
	}
}
