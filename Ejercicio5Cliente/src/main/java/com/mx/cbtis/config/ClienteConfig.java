package com.mx.cbtis.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Un solo RestTemplate compartido (antes se creaba uno nuevo en cada metodo),
 * con tiempos maximos de espera para no bloquear el frontend si la API no responde.
 */
@Configuration
public class ClienteConfig {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.setConnectTimeout(Duration.ofSeconds(5))
				.setReadTimeout(Duration.ofSeconds(10))
				.build();
	}
}
