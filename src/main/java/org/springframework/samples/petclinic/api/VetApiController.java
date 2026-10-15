package org.springframework.samples.petclinic.api;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.api.ApiDtos.VetResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/vets")
@Tag(name = "Veterinarians")
class VetApiController {

	private final ApiService service;

	VetApiController(ApiService service) {
		this.service = service;
	}

	@GetMapping
	ResponseEntity<ApiPage<VetResponse>> findVets(@ParameterObject Pageable pageable, HttpServletRequest request) {
		ApiPagination.validate(request);
		ApiPage<VetResponse> response = ApiPage.from(this.service.findVets(pageable), ApiMapper::vet);
		return ApiResponses.cacheable(response);
	}

	@GetMapping("/{vetId}")
	ResponseEntity<VetResponse> findVet(@PathVariable Integer vetId) {
		return ApiResponses.cacheable(ApiMapper.vet(this.service.getVet(vetId)));
	}

}
