package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mx.cbtis.gestor.modelo.Tareas;

/**
 * Datos de entrada para altas, cambios y bajas de tareas (DTO anti mass assignment).
 */
public record TareaRequest(
		@JsonProperty("id_tarea") Integer idTarea,
		@JsonProperty("nombre") String nombre,
		@JsonProperty("descripcion") String descripcion,
		@JsonProperty("fecha_vencimiento") String fechaVencimiento,
		@JsonProperty("lista") String lista,
		@JsonProperty("estado") String estado,
		@JsonProperty("id_proyecto") Integer idProyecto) {

	public Tareas toEntity() {
		return new Tareas(idTarea(), nombre(), descripcion(), fechaVencimiento(), lista(), estado(), idProyecto());
	}
}
