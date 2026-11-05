package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.dto.ProyectoRequest;
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
	public int addProyecto(@RequestBody ProyectoRequest request) {
		return guardar(request);
	}

	@PostMapping("/updateProyecto")
	public int updateProyecto(@RequestBody ProyectoRequest request) {
		return guardar(request);
	}

	@DeleteMapping("/deleteProyecto")
	public int deleteProyecto(@RequestBody ProyectoRequest request) {
		if (request == null || request.idProyecto() == null || !proyectosRepository.existsById(request.idProyecto())) {
			return RespuestaApi.FALLO;
		}
		proyectosRepository.deleteById(request.idProyecto());
		return RespuestaApi.EXITO;
	}

	@PostMapping("/buscarProyectos")
	public List<Proyectos> getProyectos() {
		List<Proyectos> lista = new ArrayList<>();
		proyectosRepository.findAll().forEach(lista::add);
		return lista;
	}

	private int guardar(ProyectoRequest request) {
		if (request == null) {
			return RespuestaApi.FALLO;
		}
		proyectosRepository.save(request.toEntity());
		return RespuestaApi.EXITO;
	}
}
