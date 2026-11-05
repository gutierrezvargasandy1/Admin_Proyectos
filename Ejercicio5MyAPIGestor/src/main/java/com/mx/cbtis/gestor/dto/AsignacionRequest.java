package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mx.cbtis.gestor.modelo.Asignaciones;

/**
 * Datos de entrada para altas, cambios y bajas de asignaciones.
 * Se usa un DTO (record de Java 21) en lugar de la entidad JPA para evitar
 * "mass assignment": el cliente solo puede enviar estos campos.
 */
public record AsignacionRequest(
		@JsonProperty("id_asignacion") Integer idAsignacion,
		@JsonProperty("id_usuario") Integer idUsuario,
		@JsonProperty("id_tarea") Integer idTarea,
		@JsonProperty("fecha_asignacion") String fechaAsignacion) {

	public Asignaciones toEntity() {
		return new Asignaciones(idAsignacion(), idUsuario(), idTarea(), fechaAsignacion());
	}
}
