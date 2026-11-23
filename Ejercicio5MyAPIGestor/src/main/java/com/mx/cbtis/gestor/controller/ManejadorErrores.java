package com.mx.cbtis.gestor.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Manejo centralizado de errores de la API.
 *
 * Las pruebas de endpoints de la Practica 1 detectaron que, ante datos invalidos,
 * la API respondia con el error generico de Spring (sin indicar la causa) o con
 * HTTP 500. Este manejador convierte esos casos en respuestas controladas:
 * <ul>
 * <li>400: falta un parametro, el parametro no es numerico o el cuerpo JSON falta o esta mal formado.</li>
 * <li>409: los datos violan una regla de la base de datos (campo obligatorio, correo duplicado,
 * fecha invalida o referencia a un registro inexistente).</li>
 * </ul>
 * Nota: el driver de MariaDB reporta los valores con formato invalido (error 1292, SQLState 22007,
 * p. ej. la fecha 2026-13-45) como SQLSyntaxErrorException, que Spring traduce a
 * InvalidDataAccessResourceUsageException y no a DataIntegrityViolationException; por eso
 * ambas excepciones se atienden en el mismo manejador (falla detectada en la reprueba).
 * El cuerpo siempre es JSON con {@code resultado = 0} (mismo valor de fallo que usan los endpoints),
 * el tipo de error y un mensaje legible. Nunca se expone el detalle SQL ni la traza.
 */
@RestControllerAdvice
public class ManejadorErrores {

	static final String SOLICITUD_INVALIDA = "Solicitud invalida";
	static final String DATOS_INVALIDOS = "Datos invalidos";
	static final String MENSAJE_CUERPO = "El cuerpo de la peticion falta o no es un JSON valido.";
	static final String MENSAJE_INTEGRIDAD = "Los datos no cumplen las reglas de la base de datos: "
			+ "campo obligatorio vacio, valor duplicado, formato invalido o referencia a un registro inexistente.";

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, Object>> parametroFaltante(MissingServletRequestParameterException ex) {
		return respuesta(HttpStatus.BAD_REQUEST, SOLICITUD_INVALIDA,
				"Falta el parametro obligatorio '" + ex.getParameterName() + "'.");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> tipoInvalido(MethodArgumentTypeMismatchException ex) {
		return respuesta(HttpStatus.BAD_REQUEST, SOLICITUD_INVALIDA,
				"El parametro '" + ex.getName() + "' debe ser un numero entero.");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> cuerpoInvalido() {
		return respuesta(HttpStatus.BAD_REQUEST, SOLICITUD_INVALIDA, MENSAJE_CUERPO);
	}

	@ExceptionHandler({ DataIntegrityViolationException.class, InvalidDataAccessResourceUsageException.class })
	public ResponseEntity<Map<String, Object>> integridad() {
		return respuesta(HttpStatus.CONFLICT, DATOS_INVALIDOS, MENSAJE_INTEGRIDAD);
	}

	private static ResponseEntity<Map<String, Object>> respuesta(HttpStatus estado, String error, String mensaje) {
		Map<String, Object> cuerpo = new LinkedHashMap<>();
		cuerpo.put("resultado", RespuestaApi.FALLO);
		cuerpo.put("estado", estado.value());
		cuerpo.put("error", error);
		cuerpo.put("mensaje", mensaje);
		return ResponseEntity.status(estado).body(cuerpo);
	}
}
