package com.mx.cbtis.gestor.modelo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Asignacion de una tarea a un usuario.
 * Los atributos usan camelCase (convencion Java) y se mapean explicitamente
 * a las columnas de la BD (@Column) y a las llaves del JSON (@JsonProperty)
 * para no romper el contrato con el frontend.
 */
@Entity
public class Asignaciones {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id_asignacion")
	@JsonProperty("id_asignacion")
	private Integer idAsignacion;

	@Column(name = "id_usuario")
	@JsonProperty("id_usuario")
	private Integer idUsuario;

	@Column(name = "id_tarea")
	@JsonProperty("id_tarea")
	private Integer idTarea;

	@Column(name = "fecha_asignacion")
	@JsonProperty("fecha_asignacion")
	private String fechaAsignacion;

	public Asignaciones() {
		super();
	}

	public Asignaciones(Integer idAsignacion, Integer idUsuario, Integer idTarea, String fechaAsignacion) {
		super();
		this.idAsignacion = idAsignacion;
		this.idUsuario = idUsuario;
		this.idTarea = idTarea;
		this.fechaAsignacion = fechaAsignacion;
	}

	public Integer getIdAsignacion() {
		return idAsignacion;
	}

	public void setIdAsignacion(Integer idAsignacion) {
		this.idAsignacion = idAsignacion;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Integer getIdTarea() {
		return idTarea;
	}

	public void setIdTarea(Integer idTarea) {
		this.idTarea = idTarea;
	}

	public String getFechaAsignacion() {
		return fechaAsignacion;
	}

	public void setFechaAsignacion(String fechaAsignacion) {
		this.fechaAsignacion = fechaAsignacion;
	}
}
