package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProyectosRepository;

@RestController
public class ProyectosController {

	private final ProyectosRepository proyectosRepository;

	public ProyectosController(ProyectosRepository proyectosRepository) {
		this.proyectosRepository = proyectosRepository;
	}

	@GetMapping("/buscarProyecto")
	public Proyectos getProyecto(@RequestParam("id_proyecto") int idProyecto) {
		return proyectosRepository.findById(idProyecto).orElseGet(Proyectos::new);
	}

	@PostMapping("/addProyecto")
	public int addProyecto(@RequestBody Proyectos proyecto) {
		return guardar(proyecto);
	}

	@PostMapping("/updateProyecto")
	public int updateProyecto(@RequestBody Proyectos proyecto) {
		return guardar(proyecto);
	}

	@DeleteMapping("/deleteProyecto")
	public int deleteProyecto(@RequestBody Proyectos proyecto) {
		if (proyecto == null) {
			return RespuestaApi.FALLO;
		}
		proyectosRepository.delete(proyecto);
		return RespuestaApi.EXITO;
	}

	@PostMapping("/buscarProyectos")
	public List<Proyectos> getProyectos() {
		List<Proyectos> listaProyectos = new ArrayList<>();
		proyectosRepository.findAll().forEach(listaProyectos::add);
		return listaProyectos;
	}

	private int guardar(Proyectos proyecto) {
		if (proyecto == null) {
			return RespuestaApi.FALLO;
		}
		proyectosRepository.save(proyecto);
		return RespuestaApi.EXITO;
	}
}
