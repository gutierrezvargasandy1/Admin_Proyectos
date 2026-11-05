package com.mx.cbtis.gestor.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

/**
 * Pruebas unitarias de UsuarioController.
 * Se usa un BCryptPasswordEncoder REAL (no mock) para validar el cifrado
 * de contrasenas de punta a punta; el repositorio es un mock.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

	private static final String CORREO = "ana@cbtis.mx";
	private static final String CONTRASENA = "Secreta123";

	@Mock
	private UsuarioRepository repository;

	/** Costo 4 = minimo permitido, para que las pruebas sean rapidas. */
	private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

	private UsuarioController controller;

	@BeforeEach
	void setUp() {
		controller = new UsuarioController(repository, encoder);
	}

	private Usuario usuarioGuardado() {
		return new Usuario(10, "Ana Lopez", CORREO, encoder.encode(CONTRASENA), "Estudiante", "#70FAB7");
	}

	private static Usuario credenciales(String correo, String contrasena) {
		Usuario credenciales = new Usuario();
		credenciales.setCorreoElectronico(correo);
		credenciales.setContrasena(contrasena);
		return credenciales;
	}

	// ---------------------------------------------------------------- Login

	@Test
	@DisplayName("Login: credenciales correctas regresan al usuario")
	void loginCorrecto() {
		Usuario guardado = usuarioGuardado();
		when(repository.findFirstByCorreoElectronico(CORREO)).thenReturn(Optional.of(guardado));

		Usuario resultado = controller.getUsuarioLogin(credenciales(CORREO, CONTRASENA));

		assertThat(resultado).isSameAs(guardado);
		assertThat(resultado.getNombre()).isEqualTo("Ana Lopez");
	}

	@Test
	@DisplayName("Login: contrasena incorrecta regresa nombre '000'")
	void loginContrasenaIncorrecta() {
		when(repository.findFirstByCorreoElectronico(CORREO)).thenReturn(Optional.of(usuarioGuardado()));

		Usuario resultado = controller.getUsuarioLogin(credenciales(CORREO, "otraContrasena"));

		assertThat(resultado.getNombre()).isEqualTo(UsuarioController.LOGIN_INVALIDO);
		assertThat(resultado.getIdUsuario()).isNull();
	}

	@Test
	@DisplayName("Login: correo inexistente regresa nombre '000'")
	void loginCorreoInexistente() {
		when(repository.findFirstByCorreoElectronico("nadie@cbtis.mx")).thenReturn(Optional.empty());

		Usuario resultado = controller.getUsuarioLogin(credenciales("nadie@cbtis.mx", CONTRASENA));

		assertThat(resultado.getNombre()).isEqualTo(UsuarioController.LOGIN_INVALIDO);
	}

	@Test
	@DisplayName("Login: cuerpo nulo regresa '000' sin consultar la BD")
	void loginNulo() {
		assertThat(controller.getUsuarioLogin(null).getNombre()).isEqualTo(UsuarioController.LOGIN_INVALIDO);
		verify(repository, never()).findFirstByCorreoElectronico(anyString());
	}

	@Test
	@DisplayName("Login: sin correo regresa '000' sin consultar la BD")
	void loginSinCorreo() {
		assertThat(controller.getUsuarioLogin(credenciales(null, CONTRASENA)).getNombre())
				.isEqualTo(UsuarioController.LOGIN_INVALIDO);
		verify(repository, never()).findFirstByCorreoElectronico(anyString());
	}

	@Test
	@DisplayName("Login: contrasena en blanco regresa '000' sin consultar la BD")
	void loginContrasenaEnBlanco() {
		assertThat(controller.getUsuarioLogin(credenciales(CORREO, "   ")).getNombre())
				.isEqualTo(UsuarioController.LOGIN_INVALIDO);
		verify(repository, never()).findFirstByCorreoElectronico(anyString());
	}

	@Test
	@DisplayName("Login: un usuario con contrasena en texto plano (dato heredado) NO puede entrar")
	void loginContrasenaTextoPlanoRechazada() {
		Usuario heredado = new Usuario(11, "Usuario viejo", CORREO, CONTRASENA, "Estudiante", "#000000");
		when(repository.findFirstByCorreoElectronico(CORREO)).thenReturn(Optional.of(heredado));

		Usuario resultado = controller.getUsuarioLogin(credenciales(CORREO, CONTRASENA));

		assertThat(resultado.getNombre()).isEqualTo(UsuarioController.LOGIN_INVALIDO);
	}

	// ---------------------------------------------------------------- Buscar

	@Test
	@DisplayName("Buscar usuario: regresa al usuario cuando existe")
	void buscarExistente() {
		Usuario guardado = usuarioGuardado();
		when(repository.findById(10)).thenReturn(Optional.of(guardado));

		assertThat(controller.getUsuario(10)).isSameAs(guardado);
	}

	@Test
	@DisplayName("Buscar usuario: regresa objeto vacio cuando NO existe")
	void buscarInexistente() {
		when(repository.findById(99)).thenReturn(Optional.empty());

		Usuario resultado = controller.getUsuario(99);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getIdUsuario()).isNull();
	}

	// ---------------------------------------------------------------- Alta

	@Test
	@DisplayName("Alta: guarda la contrasena CIFRADA (BCrypt), nunca en texto plano")
	void altaCifraContrasena() {
		Usuario nuevo = new Usuario(null, "Ana Lopez", CORREO, CONTRASENA, "Estudiante", "#70FAB7");
		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

		assertThat(controller.addUsuario(nuevo)).isEqualTo(RespuestaApi.EXITO);

		verify(repository).save(captor.capture());
		String hash = captor.getValue().getContrasena();
		assertThat(hash).isNotEqualTo(CONTRASENA).startsWith("$2");
		assertThat(encoder.matches(CONTRASENA, hash)).isTrue();
	}

	@Test
	@DisplayName("Alta: cuerpo nulo regresa 0 y no guarda")
	void altaNula() {
		assertThat(controller.addUsuario(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	@Test
	@DisplayName("Alta: sin contrasena regresa 0 y no guarda")
	void altaSinContrasena() {
		Usuario sinContrasena = new Usuario(null, "Ana Lopez", CORREO, null, "Estudiante", "#70FAB7");

		assertThat(controller.addUsuario(sinContrasena)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	// ---------------------------------------------------------------- Actualizacion

	@Test
	@DisplayName("Actualizacion: con contrasena nueva la guarda cifrada")
	void actualizacionConContrasenaNueva() {
		Usuario cambios = new Usuario(10, "Ana Lopez", CORREO, "NuevaClave1", "Estudiante", "#70FAB7");
		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

		assertThat(controller.updateUsuario(cambios)).isEqualTo(RespuestaApi.EXITO);

		verify(repository).save(captor.capture());
		assertThat(encoder.matches("NuevaClave1", captor.getValue().getContrasena())).isTrue();
		verify(repository, never()).findById(anyInt());
	}

	@Test
	@DisplayName("Actualizacion: sin contrasena conserva el hash guardado")
	void actualizacionSinContrasenaConservaHash() {
		Usuario guardado = usuarioGuardado();
		String hashOriginal = guardado.getContrasena();
		when(repository.findById(10)).thenReturn(Optional.of(guardado));
		Usuario cambios = new Usuario(10, "Ana L.", CORREO, null, "Docente", "#FFFFFF");

		assertThat(controller.updateUsuario(cambios)).isEqualTo(RespuestaApi.EXITO);

		assertThat(cambios.getContrasena()).isEqualTo(hashOriginal);
		verify(repository).save(cambios);
	}

	@Test
	@DisplayName("Actualizacion: sin contrasena y usuario inexistente guarda sin contrasena")
	void actualizacionSinContrasenaUsuarioInexistente() {
		when(repository.findById(50)).thenReturn(Optional.empty());
		Usuario cambios = new Usuario(50, "Nuevo", "nuevo@cbtis.mx", "", "Estudiante", "#FFFFFF");

		assertThat(controller.updateUsuario(cambios)).isEqualTo(RespuestaApi.EXITO);

		assertThat(cambios.getContrasena()).isEmpty();
		verify(repository).save(cambios);
	}

	@Test
	@DisplayName("Actualizacion: sin contrasena y sin id no consulta la BD")
	void actualizacionSinContrasenaSinId() {
		Usuario cambios = new Usuario(null, "Nuevo", "nuevo@cbtis.mx", null, "Estudiante", "#FFFFFF");

		assertThat(controller.updateUsuario(cambios)).isEqualTo(RespuestaApi.EXITO);

		verify(repository, never()).findById(anyInt());
		verify(repository).save(cambios);
	}

	@Test
	@DisplayName("Actualizacion: cuerpo nulo regresa 0 y no guarda")
	void actualizacionNula() {
		assertThat(controller.updateUsuario(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).save(any());
	}

	// ---------------------------------------------------------------- Baja y listado

	@Test
	@DisplayName("Baja: elimina al usuario y regresa 1")
	void bajaExitosa() {
		Usuario guardado = usuarioGuardado();

		assertThat(controller.deleteUsuario(guardado)).isEqualTo(RespuestaApi.EXITO);
		verify(repository).delete(guardado);
	}

	@Test
	@DisplayName("Baja: cuerpo nulo regresa 0 y no elimina")
	void bajaNula() {
		assertThat(controller.deleteUsuario(null)).isEqualTo(RespuestaApi.FALLO);
		verify(repository, never()).delete(any());
	}

	@Test
	@DisplayName("Listado: regresa todos los usuarios")
	void listadoConDatos() {
		Usuario uno = usuarioGuardado();
		Usuario dos = new Usuario();
		when(repository.findAll()).thenReturn(List.of(uno, dos));

		assertThat(controller.getUsuarios()).containsExactly(uno, dos);
	}

	@Test
	@DisplayName("Listado: regresa lista vacia cuando no hay usuarios")
	void listadoVacio() {
		when(repository.findAll()).thenReturn(Collections.emptyList());

		assertThat(controller.getUsuarios()).isEmpty();
	}
}
