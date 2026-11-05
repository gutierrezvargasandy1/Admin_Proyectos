package com.mx.cbtis.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mx.cbtis.cliente.GestorApiClient;
import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

/**
 * Controlador del frontend: sirve las vistas Thymeleaf y hace de puente
 * entre el JavaScript del navegador y la API (a traves de GestorApiClient).
 */
@Controller
public class GestorController {

	static final String VISTA_PROYECTO = "proyecto";
	static final String REDIRECT_INICIO = "redirect:/inicio";
	static final String REDIRECT_PROYECTO = "redirect:/proyecto";

	private final GestorApiClient api;

	public GestorController(GestorApiClient api) {
		this.api = api;
	}

	// ------------------------------------------------------------ Vistas

	@GetMapping("/")
	public String getHome() {
		return "home";
	}

	@GetMapping("/tutorial")
	public String getTutorial() {
		return "tutorial";
	}

	@GetMapping("/manual")
	public String getManual() {
		return "manual";
	}

	@GetMapping("/login")
	public String getLogin() {
		return "login";
	}

	@GetMapping("/register")
	public String getRegister(Model modelo) {
		modelo.addAttribute("usuario", new Usuario());
		return "register";
	}

	@GetMapping("/inicio")
	public String getInicio(Model modelo) {
		modelo.addAttribute("proyectos", new Proyectos());
		return "inicio";
	}

	@GetMapping("/proyecto")
	public String getProyecto(Model modelo) {
		modelo.addAttribute("tarea", new Tareas());
		modelo.addAttribute("comentarios", new Comentarios());
		modelo.addAttribute("listaUsuarios", api.buscarUsuarios());
		return VISTA_PROYECTO;
	}

	// ------------------------------------------------------------ Usuarios

	@PostMapping("/saveUsuario")
	public String saveUsuario(@ModelAttribute Usuario usuario) {
		api.addUsuario(usuario);
		return "redirect:/login";
	}

	@PostMapping("/verificarLogin")
	public ResponseEntity<Usuario> verificarLogin(@RequestBody Usuario credenciales) {
		return ResponseEntity.ok(api.login(credenciales));
	}

	@PostMapping("/consultarUsuarios")
	public ResponseEntity<List<Usuario>> consultarUsuarios() {
		return ResponseEntity.ok(api.buscarUsuarios());
	}

	// ------------------------------------------------------------ Proyectos

	@PostMapping("/saveProyecto")
	public String saveProyecto(@ModelAttribute Proyectos proyecto) {
		api.addProyecto(proyecto);
		return REDIRECT_INICIO;
	}

	@PostMapping("/consultarProyectos")
	public ResponseEntity<List<Proyectos>> consultarProyectos() {
		return ResponseEntity.ok(api.buscarProyectos());
	}

	@PostMapping("/actualizarProyecto")
	public ResponseEntity<Proyectos> actualizarProyecto(@RequestBody Proyectos proyecto) {
		return ResponseEntity.ok(api.buscarProyecto(proyecto.getId_proyecto()));
	}

	@PostMapping("/eliminarProyecto")
	public String eliminarProyecto(@RequestBody Proyectos proyecto) {
		api.deleteProyecto(proyecto);
		return REDIRECT_INICIO;
	}

	// ------------------------------------------------------------ Tareas

	@PostMapping("/saveTarea")
	public String saveTarea(@ModelAttribute Tareas tarea, RedirectAttributes atributos) {
		api.addTarea(tarea);
		atributos.addAttribute("id_proyecto", tarea.getId_proyecto());
		return REDIRECT_PROYECTO;
	}

	@PostMapping("/verificarTarea")
	public ResponseEntity<Integer> verificarTarea(@RequestBody Tareas tarea) {
		return ResponseEntity.ok(api.addTarea(tarea));
	}

	@PostMapping("/cambiosTareas")
	public ResponseEntity<Integer> cambiosTareas(@RequestBody Tareas tarea) {
		return verificarTarea(tarea);
	}

	@PostMapping("/obtenerTareas")
	public ResponseEntity<List<Tareas>> obtenerTareas(@RequestBody Tareas filtro) {
		return ResponseEntity.ok(api.tareasPorProyecto(filtro));
	}

	@PostMapping("/consultarTareas")
	public ResponseEntity<List<Tareas>> consultarTareas() {
		return ResponseEntity.ok(api.buscarTareas());
	}

	@PostMapping("/actualizarTarea")
	public ResponseEntity<Tareas> actualizarTarea(@RequestBody Tareas tarea) {
		return ResponseEntity.ok(api.buscarTarea(tarea.getId_tarea()));
	}

	@PostMapping("/eliminarTarea")
	public String eliminarTarea(@RequestBody Tareas tarea) {
		api.deleteTarea(tarea);
		return REDIRECT_PROYECTO;
	}

	// ------------------------------------------------------------ Comentarios

	@PostMapping("/saveComentario")
	public ResponseEntity<Integer> saveComentario(@RequestBody Comentarios comentario) {
		return ResponseEntity.ok(api.addComentario(comentario));
	}

	@PostMapping("/obtenerComentarios")
	public ResponseEntity<List<Comentarios>> obtenerComentarios(@RequestBody Comentarios filtro) {
		return ResponseEntity.ok(api.comentariosPorTarea(filtro));
	}

	@PostMapping("/consultarComentarios")
	public ResponseEntity<List<Comentarios>> consultarComentarios() {
		return ResponseEntity.ok(api.buscarComentarios());
	}
}
