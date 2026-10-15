package org.springframework.samples.petclinic.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.api.ApiDtos.PetTypeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/pet-types")
@Tag(name = "Pet types")
class PetTypeApiController {

	private final ApiService service;

	PetTypeApiController(ApiService service) {
		this.service = service;
	}

	@GetMapping
	ResponseEntity<List<PetTypeResponse>> findPetTypes() {
		List<PetTypeResponse> response = this.service.findPetTypes().stream().map(ApiMapper::petType).toList();
		return ApiResponses.cacheable(response);
	}

}
