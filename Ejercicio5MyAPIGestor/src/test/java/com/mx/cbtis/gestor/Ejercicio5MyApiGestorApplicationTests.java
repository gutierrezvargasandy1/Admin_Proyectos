package com.mx.cbtis.gestor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mx.cbtis.gestor.controller.AsignacionesController;
import com.mx.cbtis.gestor.controller.ComentariosController;
import com.mx.cbtis.gestor.controller.ProyectosController;
import com.mx.cbtis.gestor.controller.TareasController;
import com.mx.cbtis.gestor.controller.UsuarioController;

/**
 * Verifica que el contexto completo de Spring arranque (con H2)
 * y que los componentes principales esten registrados.
 */
@SpringBootTest
class Ejercicio5MyApiGestorApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	@DisplayName("El contexto carga y registra los 5 controladores")
	void contextLoads() {
		assertThat(context.getBean(UsuarioController.class)).isNotNull();
		assertThat(context.getBean(ProyectosController.class)).isNotNull();
		assertThat(context.getBean(TareasController.class)).isNotNull();
		assertThat(context.getBean(ComentariosController.class)).isNotNull();
		assertThat(context.getBean(AsignacionesController.class)).isNotNull();
	}

	@Test
	@DisplayName("El PasswordEncoder configurado es BCrypt")
	void passwordEncoderEsBCrypt() {
		String hash = passwordEncoder.encode("Clave123");

		assertThat(hash).startsWith("$2");
		assertThat(passwordEncoder.matches("Clave123", hash)).isTrue();
		assertThat(passwordEncoder.matches("Otra", hash)).isFalse();
	}
}
