package com.mx.cbtis.gestor.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.mx.cbtis.gestor.modelo.Tareas;

public interface TareasRepository extends CrudRepository<Tareas, Integer> {

	/**
	 * Tareas de un proyecto. Consulta derivada por Spring Data
	 * (reemplaza la consulta SQL nativa anterior).
	 */
	List<Tareas> findByIdProyecto(Integer idProyecto);
}
