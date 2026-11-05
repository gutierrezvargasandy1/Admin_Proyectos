package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;

@RestController
public class ComentariosController {

	private final ComentariosRepository comentariosRepository;

	public ComentariosController(ComentariosRepository comentariosRepository) {
		this.comentariosRepository = comentariosRepository;
	}

	@PostMapping("/getComentario")
	public List<Comentarios> getComentariosPorTarea(@RequestBody Comentarios filtro) {
		if (filtro == null || filtro.getIdTarea() == null) {
			return new ArrayList<>();
		}
		return comentariosRepository.findByIdTarea(filtro.getIdTarea());
	}

	@GetMapping("/buscarComentario")
	public Comentarios getComentario(@RequestParam int id) {
		return comentariosRepository.findById(id).orElseGet(Comentarios::new);
	}

	@PostMapping("/addComentario")
	public int addComentario(@RequestBody Comentarios comentario) {
		return guardar(comentario);
	}

	@PostMapping("/updateComentario")
	public int updateComentario(@RequestBody Comentarios comentario) {
		return guardar(comentario);
	}

	@DeleteMapping("/deleteComentario")
	public int deleteComentario(@RequestBody Comentarios comentario) {
		if (comentario == null) {
			return RespuestaApi.FALLO;
		}
		comentariosRepository.delete(comentario);
		return RespuestaApi.EXITO;
	}

	@PostMapping("/buscarComentarios")
	public List<Comentarios> getComentarios() {
		List<Comentarios> listaComentarios = new ArrayList<>();
		comentariosRepository.findAll().forEach(listaComentarios::add);
		return listaComentarios;
	}

	private int guardar(Comentarios comentario) {
		if (comentario == null) {
			return RespuestaApi.FALLO;
		}
		comentariosRepository.save(comentario);
		return RespuestaApi.EXITO;
	}
}
