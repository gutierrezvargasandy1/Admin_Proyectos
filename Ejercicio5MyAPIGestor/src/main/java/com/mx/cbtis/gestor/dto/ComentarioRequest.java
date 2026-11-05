package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mx.cbtis.gestor.modelo.Comentarios;

/**
 * Datos de entrada para altas, cambios y bajas de comentarios (DTO anti mass assignment).
 */
public record ComentarioRequest(
		@JsonProperty("id_comentario") Integer idComentario,
		@JsonProperty("id_usuario") String idUsuario,
		@JsonProperty("id_tarea") Integer idTarea,
		@JsonProperty("texto_comentario") String textoComentario,
		@JsonProperty("fecha_hora") String fechaHora) {

	public Comentarios toEntity() {
		return new Comentarios(idComentario(), idUsuario(), idTarea(), textoComentario(), fechaHora());
	}
}
