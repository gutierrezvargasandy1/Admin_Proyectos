package com.mx.cbtis.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

/**
 * Pruebas del contrato frontend -> API: se verifica la URL, el metodo HTTP y el JSON
 * que el frontend envia a cada endpoint del backend, y como interpreta la respuesta.
 * La API se simula con MockRestServiceServer (no se necesita el backend levantado).
 */
class GestorApiClientTest {

	private static final String API = "http://api.test";

	private MockRestServiceServer servidor;
	private GestorApiClient cliente;

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		servidor = MockRestServiceServer.bindTo(restTemplate).build();
		cliente = new GestorApiClient(restTemplate, API);
	}

	@AfterEach
	void verificarLlamadas() {
		servidor.verify();
	}

	// ------------------------------------------------------------ Usuarios

	@Test
	@DisplayName("addUsuario: POST /addUsuario con el JSON del usuario y regresa 1")
	void addUsuario() {
		servidor.expect(requestTo(API + "/addUsuario"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.correo_electronico").value("ana@cbtis.mx"))
				.andExpect(jsonPath("$.contrasena").value("Secreta123"))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

		int resultado = cliente.addUsuario(new Usuario(null, "Ana", "ana@cbtis.mx", "Secreta123", "Estudiante", "#70FAB7"));

		assertThat(resultado).isEqualTo(1);
	}

	@Test
	@DisplayName("addUsuario: si la API no responde cuerpo se toma como fallo (0)")
	void addUsuarioSinRespuesta() {
		servidor.expect(requestTo(API + "/addUsuario"))
				.andRespond(withSuccess());

		assertThat(cliente.addUsuario(new Usuario())).isZero();
	}

	@Test
	@DisplayName("login: POST /getUsuarioLogin con correo y contrasena; regresa el usuario")
	void login() {
		servidor.expect(requestTo(API + "/getUsuarioLogin"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().json("{\"correo_electronico\":\"ana@cbtis.mx\",\"contrasena\":\"Secreta123\"}"))
				.andRespond(withSuccess("{\"id_usuario\":131,\"nombre\":\"Ana\",\"correo_electronico\":\"ana@cbtis.mx\"}",
						MediaType.APPLICATION_JSON));

		Usuario credenciales = new Usuario();
		credenciales.setCorreo_electronico("ana@cbtis.mx");
		credenciales.setContrasena("Secreta123");
		Usuario usuario = cliente.login(credenciales);

		assertThat(usuario.getId_usuario()).isEqualTo(131);
		assertThat(usuario.getNombre()).isEqualTo("Ana");
		assertThat(usuario.getContrasena()).isNull();
	}

	@Test
	@DisplayName("buscarUsuarios: POST /buscarUsuarios y convierte el arreglo en lista")
	void buscarUsuarios() {
		servidor.expect(requestTo(API + "/buscarUsuarios"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("[{\"id_usuario\":1,\"nombre\":\"Ana\"},{\"id_usuario\":2,\"nombre\":\"Luis\"}]",
						MediaType.APPLICATION_JSON));

		List<Usuario> usuarios = cliente.buscarUsuarios();

		assertThat(usuarios).extracting(Usuario::getNombre).containsExactly("Ana", "Luis");
	}

	@Test
	@DisplayName("buscarUsuarios: si la API no regresa nada, lista vacia (sin NullPointerException)")
	void buscarUsuariosSinRespuesta() {
		servidor.expect(requestTo(API + "/buscarUsuarios"))
				.andRespond(withSuccess());

		assertThat(cliente.buscarUsuarios()).isEmpty();
	}

	// ------------------------------------------------------------ Proyectos

	@Test
	@DisplayName("addProyecto: POST /addProyecto con los campos en snake_case")
	void addProyecto() {
		servidor.expect(requestTo(API + "/addProyecto"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.nombre").value("My API Rest"))
				.andExpect(jsonPath("$.estado_proyecto").value("En proceso"))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

		Proyectos proyecto = new Proyectos(null, "My API Rest", "API", "2026-05-31", "2026-06-29", "En proceso", "/a.gif");

		assertThat(cliente.addProyecto(proyecto)).isEqualTo(1);
	}

	@Test
	@DisplayName("buscarProyectos: POST /buscarProyectos")
	void buscarProyectos() {
		servidor.expect(requestTo(API + "/buscarProyectos"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("[{\"id_proyecto\":197,\"nombre\":\"My API Rest\"}]", MediaType.APPLICATION_JSON));

		List<Proyectos> proyectos = cliente.buscarProyectos();

		assertThat(proyectos).hasSize(1);
		assertThat(proyectos.get(0).getId_proyecto()).isEqualTo(197);
	}

	@Test
	@DisplayName("buscarProyecto: GET /buscarProyecto?id_proyecto=197")
	void buscarProyecto() {
		servidor.expect(requestTo(API + "/buscarProyecto?id_proyecto=197"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"id_proyecto\":197,\"nombre\":\"My API Rest\"}", MediaType.APPLICATION_JSON));

		assertThat(cliente.buscarProyecto(197).getNombre()).isEqualTo("My API Rest");
	}

	@Test
	@DisplayName("deleteProyecto: DELETE /deleteProyecto con el id en el cuerpo")
	void deleteProyecto() {
		servidor.expect(requestTo(API + "/deleteProyecto"))
				.andExpect(method(HttpMethod.DELETE))
				.andExpect(jsonPath("$.id_proyecto").value(197))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

		Proyectos proyecto = new Proyectos();
		proyecto.setId_proyecto(197);

		assertThat(cliente.deleteProyecto(proyecto)).isEqualTo(1);
	}

	@Test
	@DisplayName("deleteProyecto: la API regresa 0 cuando el proyecto no existe")
	void deleteProyectoInexistente() {
		servidor.expect(requestTo(API + "/deleteProyecto"))
				.andRespond(withSuccess("0", MediaType.APPLICATION_JSON));

		assertThat(cliente.deleteProyecto(new Proyectos())).isZero();
	}

	@Test
	@DisplayName("deleteProyecto: sin cuerpo de respuesta se toma como fallo (0)")
	void deleteProyectoSinRespuesta() {
		servidor.expect(requestTo(API + "/deleteProyecto"))
				.andRespond(withSuccess());

		assertThat(cliente.deleteProyecto(new Proyectos())).isZero();
	}

	// ------------------------------------------------------------ Tareas

	@Test
	@DisplayName("addTarea: POST /addTareas")
	void addTarea() {
		servidor.expect(requestTo(API + "/addTareas"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.id_proyecto").value(197))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

		Tareas tarea = new Tareas(null, "BD", "Tablas", "2026-06-13", "pendiente", "danger", 197);

		assertThat(cliente.addTarea(tarea)).isEqualTo(1);
	}

	@Test
	@DisplayName("buscarTareas: POST /buscarTareas")
	void buscarTareas() {
		servidor.expect(requestTo(API + "/buscarTareas"))
				.andRespond(withSuccess("[{\"id_tarea\":199},{\"id_tarea\":200}]", MediaType.APPLICATION_JSON));

		assertThat(cliente.buscarTareas()).extracting(Tareas::getId_tarea).containsExactly(199, 200);
	}

	@Test
	@DisplayName("tareasPorProyecto: POST /getTarea con id_proyecto")
	void tareasPorProyecto() {
		servidor.expect(requestTo(API + "/getTarea"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.id_proyecto").value(197))
				.andRespond(withSuccess("[{\"id_tarea\":199,\"id_proyecto\":197}]", MediaType.APPLICATION_JSON));

		Tareas filtro = new Tareas();
		filtro.setId_proyecto(197);

		assertThat(cliente.tareasPorProyecto(filtro)).hasSize(1);
	}

	@Test
	@DisplayName("buscarTarea: GET /buscarTarea?id_tarea=199")
	void buscarTarea() {
		servidor.expect(requestTo(API + "/buscarTarea?id_tarea=199"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"id_tarea\":199,\"nombre\":\"Base de datos\"}", MediaType.APPLICATION_JSON));

		assertThat(cliente.buscarTarea(199).getNombre()).isEqualTo("Base de datos");
	}

	@Test
	@DisplayName("deleteTarea: DELETE /deleteTareas con el id en el cuerpo")
	void deleteTarea() {
		servidor.expect(requestTo(API + "/deleteTareas"))
				.andExpect(method(HttpMethod.DELETE))
				.andExpect(jsonPath("$.id_tarea").value(199))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

		Tareas tarea = new Tareas();
		tarea.setId_tarea(199);

		assertThat(cliente.deleteTarea(tarea)).isEqualTo(1);
	}

	// ------------------------------------------------------------ Comentarios

	@Test
	@DisplayName("addComentario: POST /addComentario")
	void addComentario() {
		servidor.expect(requestTo(API + "/addComentario"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.texto_comentario").value("Subirlo YA"))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

		Comentarios comentario = new Comentarios(null, "Ana", 199, "Subirlo YA", "2026-06-26 13:48");

		assertThat(cliente.addComentario(comentario)).isEqualTo(1);
	}

	@Test
	@DisplayName("buscarComentarios: POST /buscarComentarios")
	void buscarComentarios() {
		servidor.expect(requestTo(API + "/buscarComentarios"))
				.andRespond(withSuccess("[{\"id_comentario\":201}]", MediaType.APPLICATION_JSON));

		assertThat(cliente.buscarComentarios()).extracting(Comentarios::getId_comentario).containsExactly(201);
	}

	@Test
	@DisplayName("comentariosPorTarea: POST /getComentario con id_tarea")
	void comentariosPorTarea() {
		servidor.expect(requestTo(API + "/getComentario"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.id_tarea").value(199))
				.andRespond(withSuccess("[{\"id_comentario\":201,\"id_tarea\":199}]", MediaType.APPLICATION_JSON));

		Comentarios filtro = new Comentarios();
		filtro.setId_tarea(199);

		assertThat(cliente.comentariosPorTarea(filtro)).hasSize(1);
	}
}
