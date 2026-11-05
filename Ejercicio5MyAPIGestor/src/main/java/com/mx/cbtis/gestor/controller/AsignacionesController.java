package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.dto.AsignacionRequest;
import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;

@RestController
public class AsignacionesController {

	private final AsignacionesRepository asignacionesRepository;

	public AsignacionesController(AsignacionesRepository asignacionesRepository) {
		this.asignacionesRepository = asignacionesRepository;
	}

	@GetMapping("/buscarAsignacion")
	public Asignaciones getAsignacion(@RequestParam int id) {
		return asignacionesRepository.findById(id).orElseGet(Asignaciones::new);
	}

	@PostMapping("/addAsignacion")
	public int addAsignacion(@RequestBody AsignacionRequest request) {
		return guardar(request);
	}

	@PostMapping("/updateAsignacion")
	public int updateAsignacion(@RequestBody AsignacionRequest request) {
		return guardar(request);
	}

	@DeleteMapping("/deleteAsignacion")
	public int deleteAsignacion(@RequestBody AsignacionRequest request) {
		if (request == null || request.idAsignacion() == null || !asignacionesRepository.existsById(request.idAsignacion())) {
			return RespuestaApi.FALLO;
		}
		asignacionesRepository.deleteById(request.idAsignacion());
		return RespuestaApi.EXITO;
	}

	@PostMapping("/buscarAsignaciones")
	public List<Asignaciones> getAsignaciones() {
		List<Asignaciones> lista = new ArrayList<>();
		asignacionesRepository.findAll().forEach(lista::add);
		return lista;
	}

	private int guardar(AsignacionRequest request) {
		if (request == null) {
			return RespuestaApi.FALLO;
		}
		asignacionesRepository.save(request.toEntity());
		return RespuestaApi.EXITO;
	}
}
