package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.TareasRepository;

@RestController
public class TareasController {

	private final TareasRepository tareasRepository;

	public TareasController(TareasRepository tareasRepository) {
		this.tareasRepository = tareasRepository;
	}

	@PostMapping("/getTarea")
	public List<Tareas> getTareasPorProyecto(@RequestBody Tareas filtro) {
		if (filtro == null || filtro.getIdProyecto() == null) {
			return new ArrayList<>();
		}
		return tareasRepository.findByIdProyecto(filtro.getIdProyecto());
	}

	@GetMapping("/buscarTarea")
	public Tareas getTarea(@RequestParam("id_tarea") int idTarea) {
		return tareasRepository.findById(idTarea).orElseGet(Tareas::new);
	}

	@PostMapping("/addTareas")
	public int addTareas(@RequestBody Tareas tarea) {
		return guardar(tarea);
	}

	@PostMapping("/updateTareas")
	public int updateTareas(@RequestBody Tareas tarea) {
		return guardar(tarea);
	}

	@DeleteMapping("/deleteTareas")
	public int deleteTareas(@RequestBody Tareas tarea) {
		if (tarea == null) {
			return RespuestaApi.FALLO;
		}
		tareasRepository.delete(tarea);
		return RespuestaApi.EXITO;
	}

	@PostMapping("/buscarTareas")
	public List<Tareas> getTareas() {
		List<Tareas> listaTareas = new ArrayList<>();
		tareasRepository.findAll().forEach(listaTareas::add);
		return listaTareas;
	}

	private int guardar(Tareas tarea) {
		if (tarea == null) {
			return RespuestaApi.FALLO;
		}
		tareasRepository.save(tarea);
		return RespuestaApi.EXITO;
	}
}
