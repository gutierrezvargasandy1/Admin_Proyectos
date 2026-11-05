package com.mx.cbtis.cliente;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

/**
 * Cliente HTTP de la API del Gestor de Proyectos.
 * Centraliza TODAS las llamadas al backend: antes las URLs "http://localhost:8081/..."
 * estaban repetidas y fijas en cada metodo del controlador.
 * La URL base se configura con la propiedad api.gestor.url (variable de entorno API_URL).
 */
@Component
public class GestorApiClient {

	private final RestTemplate restTemplate;
	private final String baseUrl;

	public GestorApiClient(RestTemplate restTemplate, @Value("${api.gestor.url}") String baseUrl) {
		this.restTemplate = restTemplate;
		this.baseUrl = baseUrl;
	}

	// ------------------------------------------------------------ Usuarios

	public int addUsuario(Usuario usuario) {
		return post("/addUsuario", usuario);
	}

	public Usuario login(Usuario credenciales) {
		return restTemplate.postForObject(url("/getUsuarioLogin"), credenciales, Usuario.class);
	}

	public List<Usuario> buscarUsuarios() {
		return lista(restTemplate.postForObject(url("/buscarUsuarios"), null, Usuario[].class));
	}

	// ------------------------------------------------------------ Proyectos

	public int addProyecto(Proyectos proyecto) {
		return post("/addProyecto", proyecto);
	}

	public List<Proyectos> buscarProyectos() {
		return lista(restTemplate.postForObject(url("/buscarProyectos"), null, Proyectos[].class));
	}

	public Proyectos buscarProyecto(Integer idProyecto) {
		return restTemplate.getForObject(url("/buscarProyecto?id_proyecto={id}"), Proyectos.class, idProyecto);
	}

	public int deleteProyecto(Proyectos proyecto) {
		return delete("/deleteProyecto", proyecto);
	}

	// ------------------------------------------------------------ Tareas

	public int addTarea(Tareas tarea) {
		return post("/addTareas", tarea);
	}

	public List<Tareas> buscarTareas() {
		return lista(restTemplate.postForObject(url("/buscarTareas"), null, Tareas[].class));
	}

	public List<Tareas> tareasPorProyecto(Tareas filtro) {
		return lista(restTemplate.postForObject(url("/getTarea"), filtro, Tareas[].class));
	}

	public Tareas buscarTarea(Integer idTarea) {
		return restTemplate.getForObject(url("/buscarTarea?id_tarea={id}"), Tareas.class, idTarea);
	}

	public int deleteTarea(Tareas tarea) {
		return delete("/deleteTareas", tarea);
	}

	// ------------------------------------------------------------ Comentarios

	public int addComentario(Comentarios comentario) {
		return post("/addComentario", comentario);
	}

	public List<Comentarios> buscarComentarios() {
		return lista(restTemplate.postForObject(url("/buscarComentarios"), null, Comentarios[].class));
	}

	public List<Comentarios> comentariosPorTarea(Comentarios filtro) {
		return lista(restTemplate.postForObject(url("/getComentario"), filtro, Comentarios[].class));
	}

	// ------------------------------------------------------------ Utilidades

	private String url(String ruta) {
		return baseUrl + ruta;
	}

	private int post(String ruta, Object cuerpo) {
		return codigo(restTemplate.postForObject(url(ruta), cuerpo, Integer.class));
	}

	private int delete(String ruta, Object cuerpo) {
		ResponseEntity<Integer> respuesta = restTemplate.exchange(url(ruta), HttpMethod.DELETE,
				new HttpEntity<>(cuerpo), Integer.class);
		return codigo(respuesta.getBody());
	}

	/** La API responde 1 (exito) o 0 (fallo); si no responde nada se toma como fallo. */
	private static int codigo(Integer valor) {
		return valor == null ? 0 : valor;
	}

	/** Convierte la respuesta en lista; si la API no regresa nada, lista vacia (evita NullPointerException). */
	private static <T> List<T> lista(T[] arreglo) {
		return arreglo == null ? Collections.emptyList() : Arrays.asList(arreglo);
	}
}
