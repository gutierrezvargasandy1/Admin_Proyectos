package com.mx.cbtis.gestor.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.mx.cbtis.gestor.modelo.Usuario;

public interface UsuarioRepository extends CrudRepository<Usuario, Integer> {

	/**
	 * Busca un usuario solo por correo. La contrasena ya NO se compara en SQL
	 * (antes se comparaba en texto plano); ahora se valida contra el hash BCrypt
	 * en el controlador.
	 */
	Optional<Usuario> findFirstByCorreoElectronico(String correoElectronico);
}
