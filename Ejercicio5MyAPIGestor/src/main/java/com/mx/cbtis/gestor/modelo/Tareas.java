package com.mx.cbtis.gestor.modelo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tarea perteneciente a un proyecto.
 */
@Entity
public class Tareas {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id_tarea")
	@JsonProperty("id_tarea")
	private Integer idTarea;

	private String nombre;

	private String descripcion;

	@Column(name = "fecha_vencimiento")
	@JsonProperty("fecha_vencimiento")
	private String fechaVencimiento;

	private String lista;

	private String estado;

	@Column(name = "id_proyecto")
	@JsonProperty("id_proyecto")
	private Integer idProyecto;

	public Tareas() {
		super();
	}

	public Tareas(Integer idTarea, String nombre, String descripcion, String fechaVencimiento, String lista,
			String estado, Integer idProyecto) {
		super();
		this.idTarea = idTarea;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.fechaVencimiento = fechaVencimiento;
		this.lista = lista;
		this.estado = estado;
		this.idProyecto = idProyecto;
	}

	public Integer getIdTarea() {
		return idTarea;
	}

	public void setIdTarea(Integer idTarea) {
		this.idTarea = idTarea;
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

	public String getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(String fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public String getLista() {
		return lista;
	}

	public void setLista(String lista) {
		this.lista = lista;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Integer getIdProyecto() {
		return idProyecto;
	}

	public void setIdProyecto(Integer idProyecto) {
		this.idProyecto = idProyecto;
	}
}
