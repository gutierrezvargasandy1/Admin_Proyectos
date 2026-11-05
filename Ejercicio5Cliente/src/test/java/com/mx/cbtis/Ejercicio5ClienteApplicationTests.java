package com.mx.cbtis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.mx.cbtis.cliente.GestorApiClient;
import com.mx.cbtis.controller.GestorController;

/**
 * Arranca el frontend completo (sin backend) y verifica que las paginas
 * Thymeleaf principales se generen correctamente (HTTP 200 con HTML).
 */
@SpringBootTest
@AutoConfigureMockMvc
class Ejercicio5ClienteApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private GestorApiClient gestorApiClient;

	@Autowired
	private GestorController gestorController;

	@Test
	@DisplayName("El contexto carga con RestTemplate, cliente de la API y controlador")
	void contextLoads() {
		assertThat(restTemplate).isNotNull();
		assertThat(gestorApiClient).isNotNull();
		assertThat(gestorController).isNotNull();
	}

	@Test
	@DisplayName("Las paginas publicas se generan sin errores")
	void paginasPublicasSeGeneran() throws Exception {
		for (String ruta : new String[] { "/", "/login", "/register", "/inicio", "/tutorial", "/manual" }) {
			mockMvc.perform(get(ruta))
					.andExpect(status().isOk())
					.andExpect(content().string(Matchers.containsStringIgnoringCase("<html")));
		}
	}
}
