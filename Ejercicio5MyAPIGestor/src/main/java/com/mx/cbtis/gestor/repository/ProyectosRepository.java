package com.mx.cbtis.gestor.repository;

import org.springframework.data.repository.CrudRepository;

import com.mx.cbtis.gestor.modelo.Proyectos;

/**
 * Antes se llamaba "ProtectosRepository" (error de escritura). Se renombro.
 */
public interface ProyectosRepository extends CrudRepository<Proyectos, Integer> {

}
