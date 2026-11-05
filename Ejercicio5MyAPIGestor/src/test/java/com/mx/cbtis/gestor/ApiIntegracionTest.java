package com.mx.cbtis.gestor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

/**
 * Prueba de integracion de punta a punta: HTTP -> controlador -> repositorio -> BD (H2).
 * Registra un usuario, verifica que la contrasena quede cifrada en la BD
 * y que el login funcione solo con la contrasena correcta.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegracionTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Test
	@DisplayName("Registro + login: contrasena cifrada en BD y login correcto/incorrecto")
	void registroYLogin() throws Exception {
		mockMvc.perform(post("/addUsuario")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Integracion\",\"correo_electronico\":\"integracion@cbtis.mx\","
						+ "\"contrasena\":\"ClaveSegura1\",\"rol\":\"Estudiante\",\"imagen_usuario\":\"#123456\"}"))
				.andExpect(status().isOk())
				.andExpect(content().string("1"));

		Usuario guardado = usuarioRepository.findFirstByCorreoElectronico("integracion@cbtis.mx").orElseThrow();
		assertThat(guardado.getContrasena()).isNotEqualTo("ClaveSegura1").startsWith("$2");

		mockMvc.perform(post("/getUsuarioLogin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"integracion@cbtis.mx\",\"contrasena\":\"ClaveSegura1\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Integracion"))
				.andExpect(jsonPath("$.contrasena").doesNotExist());

		mockMvc.perform(post("/getUsuarioLogin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"integracion@cbtis.mx\",\"contrasena\":\"Incorrecta\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("000"));
	}
}
