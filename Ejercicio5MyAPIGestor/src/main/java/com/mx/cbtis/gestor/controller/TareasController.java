package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.dto.FiltroProyectoRequest;
import com.mx.cbtis.gestor.dto.TareaRequest;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.TareasRepository;

@RestController
public class TareasController {

	private final TareasRepository tareasRepository;

	public TareasController(TareasRepository tareasRepository) {
		this.tareasRepository = tareasRepository;
	}

	@PostMapping("/getTarea")
	public List<Tareas> getTareasPorProyecto(@RequestBody FiltroProyectoRequest filtro) {
		if (filtro == null || filtro.idProyecto() == null) {
			return new ArrayList<>();
		}
		return tareasRepository.findByIdProyecto(filtro.idProyecto());
	}

	@GetMapping("/buscarTarea")
	public Tareas getTarea(@RequestParam("id_tarea") int idTarea) {
		return tareasRepository.findById(idTarea).orElseGet(Tareas::new);
	}

	@PostMapping("/addTareas")
	public int addTareas(@RequestBody TareaRequest request) {
		return guardar(request);
	}

	@PostMapping("/updateTareas")
	public int updateTareas(@RequestBody TareaRequest request) {
		return guardar(request);
	}

	@DeleteMapping("/deleteTareas")
	public int deleteTareas(@RequestBody TareaRequest request) {
		if (request == null || request.idTarea() == null || !tareasRepository.existsById(request.idTarea())) {
			return RespuestaApi.FALLO;
		}
		tareasRepository.deleteById(request.idTarea());
		return RespuestaApi.EXITO;
	}

	@PostMapping("/buscarTareas")
	public List<Tareas> getTareas() {
		List<Tareas> lista = new ArrayList<>();
		tareasRepository.findAll().forEach(lista::add);
		return lista;
	}

	private int guardar(TareaRequest request) {
		if (request == null) {
			return RespuestaApi.FALLO;
		}
		tareasRepository.save(request.toEntity());
		return RespuestaApi.EXITO;
	}
}
