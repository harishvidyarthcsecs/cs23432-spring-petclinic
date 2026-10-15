package org.springframework.samples.petclinic.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration(proxyBeanMethods = false)
class OpenApiConfiguration {

	@Bean
	OpenAPI petClinicOpenApi() {
		return new OpenAPI().info(new Info().title("Spring PetClinic API")
			.description("Versioned REST API for owners, pets, visits, veterinarians, and pet types")
			.version("v1"));
	}

}
