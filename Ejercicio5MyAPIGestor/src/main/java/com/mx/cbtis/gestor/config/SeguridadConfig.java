package com.mx.cbtis.gestor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuracion del cifrado de contrasenas.
 * Usa solo spring-security-crypto (no activa la seguridad web de Spring).
 */
@Configuration
public class SeguridadConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
