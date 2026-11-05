package com.mx.cbtis.gestor.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.mx.cbtis.gestor.modelo.Comentarios;

public interface ComentariosRepository extends CrudRepository<Comentarios, Integer> {

	/**
	 * Comentarios de una tarea. Consulta derivada por Spring Data
	 * (reemplaza la consulta SQL nativa anterior).
	 */
	List<Comentarios> findByIdTarea(Integer idTarea);
}
