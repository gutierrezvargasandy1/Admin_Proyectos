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

import com.mx.cbtis.gestor.dto.CredencialesRequest;
import com.mx.cbtis.gestor.dto.UsuarioRequest;
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
	public Usuario getUsuarioLogin(@RequestBody CredencialesRequest credenciales) {
		if (credenciales == null || !tieneTexto(credenciales.correoElectronico())
				|| !tieneTexto(credenciales.contrasena())) {
			return usuarioNoValido();
		}

		Optional<Usuario> encontrado = usuarioRepository
				.findFirstByCorreoElectronico(credenciales.correoElectronico());

		if (encontrado.isPresent()
				&& passwordEncoder.matches(credenciales.contrasena(), encontrado.get().getContrasena())) {
			return encontrado.get();
		}
		return usuarioNoValido();
	}

	@GetMapping("/buscarUsuario")
	public Usuario getUsuario(@RequestParam int id) {
		return usuarioRepository.findById(id).orElseGet(Usuario::new);
	}

	@PostMapping("/addUsuario")
	public int addUsuario(@RequestBody UsuarioRequest request) {
		if (request == null || !tieneTexto(request.contrasena())) {
			return RespuestaApi.FALLO;
		}
		Usuario usuario = request.toEntity();
		usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
		usuarioRepository.save(usuario);
		return RespuestaApi.EXITO;
	}

	@PostMapping("/updateUsuario")
	public int updateUsuario(@RequestBody UsuarioRequest request) {
		if (request == null) {
			return RespuestaApi.FALLO;
		}
		Usuario usuario = request.toEntity();
		if (tieneTexto(request.contrasena())) {
			usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
		} else {
			conservarContrasenaActual(usuario);
		}
		usuarioRepository.save(usuario);
		return RespuestaApi.EXITO;
	}

	@DeleteMapping("/deleteUsuario")
	public int deleteUsuario(@RequestBody UsuarioRequest request) {
		if (request == null || request.idUsuario() == null || !usuarioRepository.existsById(request.idUsuario())) {
			return RespuestaApi.FALLO;
		}
		usuarioRepository.deleteById(request.idUsuario());
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
