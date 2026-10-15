package org.springframework.samples.petclinic.api;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.api.ApiDtos.OwnerRequest;
import org.springframework.samples.petclinic.api.ApiDtos.OwnerResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/owners")
@Tag(name = "Owners")
class OwnerApiController {

	private final ApiService service;

	OwnerApiController(ApiService service) {
		this.service = service;
	}

	@GetMapping
	ResponseEntity<ApiPage<OwnerResponse>> findOwners(@RequestParam(defaultValue = "") String lastName,
			@ParameterObject Pageable pageable, HttpServletRequest request) {
		ApiPagination.validate(request);
		ApiPage<OwnerResponse> response = ApiPage.from(this.service.findOwners(lastName, pageable), ApiMapper::owner);
		return ApiResponses.cacheable(response);
	}

	@GetMapping("/{ownerId}")
	ResponseEntity<OwnerResponse> findOwner(@PathVariable Integer ownerId) {
		return ApiResponses.cacheable(ApiMapper.owner(this.service.getOwner(ownerId)));
	}

	@PostMapping
	ResponseEntity<OwnerResponse> createOwner(@Valid @RequestBody OwnerRequest request) {
		OwnerResponse response = ApiMapper.owner(this.service.createOwner(request));
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(response.id())
			.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@PutMapping("/{ownerId}")
	OwnerResponse updateOwner(@PathVariable Integer ownerId, @Valid @RequestBody OwnerRequest request) {
		return ApiMapper.owner(this.service.updateOwner(ownerId, request));
	}

	@DeleteMapping("/{ownerId}")
	ResponseEntity<Void> deleteOwner(@PathVariable Integer ownerId) {
		this.service.deleteOwner(ownerId);
		return ResponseEntity.noContent().build();
	}

}
