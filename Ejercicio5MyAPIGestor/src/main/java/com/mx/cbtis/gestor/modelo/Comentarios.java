package com.mx.cbtis.gestor.modelo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Comentario realizado sobre una tarea.
 * Nota: id_usuario guarda el nombre del autor (String), se respeta el esquema existente.
 */
@Entity
public class Comentarios {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id_comentario")
	@JsonProperty("id_comentario")
	private Integer idComentario;

	@Column(name = "id_usuario")
	@JsonProperty("id_usuario")
	private String idUsuario;

	@Column(name = "id_tarea")
	@JsonProperty("id_tarea")
	private Integer idTarea;

	@Column(name = "texto_comentario")
	@JsonProperty("texto_comentario")
	private String textoComentario;

	@Column(name = "fecha_hora")
	@JsonProperty("fecha_hora")
	private String fechaHora;

	public Comentarios() {
		super();
	}

	public Comentarios(Integer idComentario, String idUsuario, Integer idTarea, String textoComentario,
			String fechaHora) {
		super();
		this.idComentario = idComentario;
		this.idUsuario = idUsuario;
		this.idTarea = idTarea;
		this.textoComentario = textoComentario;
		this.fechaHora = fechaHora;
	}

	public Integer getIdComentario() {
		return idComentario;
	}

	public void setIdComentario(Integer idComentario) {
		this.idComentario = idComentario;
	}

	public String getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(String idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Integer getIdTarea() {
		return idTarea;
	}

	public void setIdTarea(Integer idTarea) {
		this.idTarea = idTarea;
	}

	public String getTextoComentario() {
		return textoComentario;
	}

	public void setTextoComentario(String textoComentario) {
		this.textoComentario = textoComentario;
	}

	public String getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(String fechaHora) {
		this.fechaHora = fechaHora;
	}
}
