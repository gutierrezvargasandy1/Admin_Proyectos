package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.dto.ComentarioRequest;
import com.mx.cbtis.gestor.dto.FiltroTareaRequest;
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;

@RestController
public class ComentariosController {

	private final ComentariosRepository comentariosRepository;

	public ComentariosController(ComentariosRepository comentariosRepository) {
		this.comentariosRepository = comentariosRepository;
	}

	@PostMapping("/getComentario")
	public List<Comentarios> getComentariosPorTarea(@RequestBody FiltroTareaRequest filtro) {
		if (filtro == null || filtro.idTarea() == null) {
			return new ArrayList<>();
		}
		return comentariosRepository.findByIdTarea(filtro.idTarea());
	}

	@GetMapping("/buscarComentario")
	public Comentarios getComentario(@RequestParam int id) {
		return comentariosRepository.findById(id).orElseGet(Comentarios::new);
	}

	@PostMapping("/addComentario")
	public int addComentario(@RequestBody ComentarioRequest request) {
		return guardar(request);
	}

	@PostMapping("/updateComentario")
	public int updateComentario(@RequestBody ComentarioRequest request) {
		return guardar(request);
	}

	@DeleteMapping("/deleteComentario")
	public int deleteComentario(@RequestBody ComentarioRequest request) {
		if (request == null || request.idComentario() == null || !comentariosRepository.existsById(request.idComentario())) {
			return RespuestaApi.FALLO;
		}
		comentariosRepository.deleteById(request.idComentario());
		return RespuestaApi.EXITO;
	}

	@PostMapping("/buscarComentarios")
	public List<Comentarios> getComentarios() {
		List<Comentarios> lista = new ArrayList<>();
		comentariosRepository.findAll().forEach(lista::add);
		return lista;
	}

	private int guardar(ComentarioRequest request) {
		if (request == null) {
			return RespuestaApi.FALLO;
		}
		comentariosRepository.save(request.toEntity());
		return RespuestaApi.EXITO;
	}
}
