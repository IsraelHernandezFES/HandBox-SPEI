package com.sandbox.spei.Validation;

import com.fasterxml.jackson.databind.ObjectMapper; // <-- Importante agregar este import
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // <-- Importante agregar este import

@SpringBootApplication
public class
ValidationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ValidationApplication.class, args);
	}

//	// --- AGREGA ESTE BLOQUE DE CÓDIGO ---
//	@Bean
//	public ObjectMapper objectMapper() {
//		return new ObjectMapper();
//	}
//	// ------------------------------------
}