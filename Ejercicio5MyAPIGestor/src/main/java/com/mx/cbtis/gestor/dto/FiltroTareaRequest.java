package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Filtro para consultar los comentarios de una tarea.
 */
public record FiltroTareaRequest(@JsonProperty("id_tarea") Integer idTarea) {
}
