package com.mx.cbtis.gestor.modelo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Proyecto del gestor.
 */
@Entity
public class Proyectos {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id_proyecto")
	@JsonProperty("id_proyecto")
	private Integer idProyecto;

	private String nombre;

	private String descripcion;

	@Column(name = "fecha_inicio")
	@JsonProperty("fecha_inicio")
	private String fechaInicio;

	@Column(name = "fecha_finalizacion")
	@JsonProperty("fecha_finalizacion")
	private String fechaFinalizacion;

	@Column(name = "estado_proyecto")
	@JsonProperty("estado_proyecto")
	private String estadoProyecto;

	private String fondo;

	public Proyectos() {
		super();
	}

	public Proyectos(Integer idProyecto, String nombre, String descripcion, String fechaInicio,
			String fechaFinalizacion, String estadoProyecto, String fondo) {
		super();
		this.idProyecto = idProyecto;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.fechaInicio = fechaInicio;
		this.fechaFinalizacion = fechaFinalizacion;
		this.estadoProyecto = estadoProyecto;
		this.fondo = fondo;
	}

	public Integer getIdProyecto() {
		return idProyecto;
	}

	public void setIdProyecto(Integer idProyecto) {
		this.idProyecto = idProyecto;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public String getFechaFinalizacion() {
		return fechaFinalizacion;
	}

	public void setFechaFinalizacion(String fechaFinalizacion) {
		this.fechaFinalizacion = fechaFinalizacion;
	}

	public String getEstadoProyecto() {
		return estadoProyecto;
	}

	public void setEstadoProyecto(String estadoProyecto) {
		this.estadoProyecto = estadoProyecto;
	}

	public String getFondo() {
		return fondo;
	}

	public void setFondo(String fondo) {
		this.fondo = fondo;
	}
}
