package org.springframework.samples.petclinic.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.api.ApiDtos.VisitRequest;
import org.springframework.samples.petclinic.api.ApiDtos.VisitResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pets/{petId}/visits")
@Tag(name = "Visits")
class VisitApiController {

	private final ApiService service;

	VisitApiController(ApiService service) {
		this.service = service;
	}

	@GetMapping
	ResponseEntity<List<VisitResponse>> findVisits(@PathVariable Integer petId) {
		List<VisitResponse> response = this.service.findPetVisits(petId)
			.stream()
			.map(visit -> ApiMapper.visit(visit, petId))
			.toList();
		return ApiResponses.cacheable(response);
	}

	@PostMapping
	ResponseEntity<VisitResponse> createVisit(@PathVariable Integer petId, @Valid @RequestBody VisitRequest request) {
		VisitResponse response = ApiMapper.visit(this.service.createVisit(petId, request), petId);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(response.id())
			.toUri();
		return ResponseEntity.created(location).body(response);
	}

}
