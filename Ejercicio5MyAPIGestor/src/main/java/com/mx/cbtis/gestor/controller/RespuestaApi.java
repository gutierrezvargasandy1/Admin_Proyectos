package com.mx.cbtis.gestor.controller;

/**
 * Codigos de respuesta que la API devuelve al frontend en altas,
 * actualizaciones y bajas (se conserva el contrato original: 1 = exito, 0 = fallo).
 */
public final class RespuestaApi {

	public static final int EXITO = 1;
	public static final int FALLO = 0;

	private RespuestaApi() {
	}
}
