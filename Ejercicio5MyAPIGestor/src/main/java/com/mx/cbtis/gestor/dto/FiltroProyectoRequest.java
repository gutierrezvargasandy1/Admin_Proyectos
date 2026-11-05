package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Filtro para consultar las tareas de un proyecto.
 */
public record FiltroProyectoRequest(@JsonProperty("id_proyecto") Integer idProyecto) {
}
