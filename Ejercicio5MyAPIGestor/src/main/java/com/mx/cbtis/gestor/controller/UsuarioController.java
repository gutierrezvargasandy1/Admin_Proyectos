package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@RestController
public class UsuarioController {

	/** Valor que el frontend interpreta como "credenciales invalidas". */
	static final String LOGIN_INVALIDO = "000";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/getUsuarioLogin")
	public Usuario getUsuarioLogin(@RequestBody Usuario credenciales) {
		if (credenciales == null || !tieneTexto(credenciales.getCorreoElectronico())
				|| !tieneTexto(credenciales.getContrasena())) {
			return usuarioNoValido();
		}

		Optional<Usuario> encontrado = usuarioRepository
				.findFirstByCorreoElectronico(credenciales.getCorreoElectronico());

		if (encontrado.isPresent()
				&& passwordEncoder.matches(credenciales.getContrasena(), encontrado.get().getContrasena())) {
			return encontrado.get();
		}
		return usuarioNoValido();
	}

	@GetMapping("/buscarUsuario")
	public Usuario getUsuario(@RequestParam int id) {
		return usuarioRepository.findById(id).orElseGet(Usuario::new);
	}

	@PostMapping("/addUsuario")
	public int addUsuario(@RequestBody Usuario usuario) {
		if (usuario == null || !tieneTexto(usuario.getContrasena())) {
			return RespuestaApi.FALLO;
		}
		usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
		usuarioRepository.save(usuario);
		return RespuestaApi.EXITO;
	}

	@PostMapping("/updateUsuario")
	public int updateUsuario(@RequestBody Usuario usuario) {
		if (usuario == null) {
			return RespuestaApi.FALLO;
		}
		if (tieneTexto(usuario.getContrasena())) {
			usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
		} else {
			conservarContrasenaActual(usuario);
		}
		usuarioRepository.save(usuario);
		return RespuestaApi.EXITO;
	}

	@DeleteMapping("/deleteUsuario")
	public int deleteUsuario(@RequestBody Usuario usuario) {
		if (usuario == null) {
			return RespuestaApi.FALLO;
		}
		usuarioRepository.delete(usuario);
		return RespuestaApi.EXITO;
	}

	@PostMapping("/buscarUsuarios")
	public List<Usuario> getUsuarios() {
		List<Usuario> listaUsuarios = new ArrayList<>();
		usuarioRepository.findAll().forEach(listaUsuarios::add);
		return listaUsuarios;
	}

	/**
	 * Si al actualizar no se envia contrasena, se conserva el hash guardado
	 * para no dejar al usuario sin acceso.
	 */
	private void conservarContrasenaActual(Usuario usuario) {
		if (usuario.getIdUsuario() == null) {
			return;
		}
		Optional<Usuario> actual = usuarioRepository.findById(usuario.getIdUsuario());
		if (actual.isPresent()) {
			usuario.setContrasena(actual.get().getContrasena());
		}
	}

	private static Usuario usuarioNoValido() {
		Usuario usuario = new Usuario();
		usuario.setNombre(LOGIN_INVALIDO);
		return usuario;
	}

	private static boolean tieneTexto(String valor) {
		return valor != null && !valor.isBlank();
	}
}
