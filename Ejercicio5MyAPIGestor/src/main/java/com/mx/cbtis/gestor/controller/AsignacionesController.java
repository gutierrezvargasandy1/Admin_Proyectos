package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	public int addAsignacion(@RequestBody Asignaciones asignacion) {
		return guardar(asignacion);
	}

	@PostMapping("/updateAsignacion")
	public int updateAsignacion(@RequestBody Asignaciones asignacion) {
		return guardar(asignacion);
	}

	@DeleteMapping("/deleteAsignacion")
	public int deleteAsignacion(@RequestBody Asignaciones asignacion) {
		if (asignacion == null) {
			return RespuestaApi.FALLO;
		}
		asignacionesRepository.delete(asignacion);
		return RespuestaApi.EXITO;
	}

	@PostMapping("/buscarAsignaciones")
	public List<Asignaciones> getAsignaciones() {
		List<Asignaciones> listaAsignaciones = new ArrayList<>();
		asignacionesRepository.findAll().forEach(listaAsignaciones::add);
		return listaAsignaciones;
	}

	private int guardar(Asignaciones asignacion) {
		if (asignacion == null) {
			return RespuestaApi.FALLO;
		}
		asignacionesRepository.save(asignacion);
		return RespuestaApi.EXITO;
	}
}
