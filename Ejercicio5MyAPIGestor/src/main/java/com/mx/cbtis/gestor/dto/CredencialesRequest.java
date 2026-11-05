package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Datos de entrada del login: solo correo y contrasena.
 */
public record CredencialesRequest(
		@JsonProperty("correo_electronico") String correoElectronico,
		@JsonProperty("contrasena") String contrasena) {
}
