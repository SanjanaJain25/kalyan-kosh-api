package com.example.kalyan_kosh_api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class KalyanKoshApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(KalyanKoshApiApplication.class, args);
	}

	/**
	 * List all registered endpoints on startup
	 */
	@Bean
	public CommandLineRunner listEndpoints(RequestMappingHandlerMapping mapping) {
		return args -> {
			System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
			System.out.println("║          📍 REGISTERED API ENDPOINTS                        ║");
			System.out.println("╠══════════════════════════════════════════════════════════════╣");

			mapping.getHandlerMethods().forEach((key, value) -> {
				String pattern = key.getPathPatternsCondition() != null
						? key.getPathPatternsCondition().toString()
						: key.getPatternsCondition().toString();
				String methods = key.getMethodsCondition().toString();

				System.out.println("║  " + methods + " " + pattern);
			});

			System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
		};
	}
}
