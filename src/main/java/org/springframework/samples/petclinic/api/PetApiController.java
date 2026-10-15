package org.springframework.samples.petclinic.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.api.ApiDtos.PetRequest;
import org.springframework.samples.petclinic.api.ApiDtos.PetResponse;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Pets")
class PetApiController {

	private final ApiService service;

	PetApiController(ApiService service) {
		this.service = service;
	}

	@GetMapping("/owners/{ownerId}/pets")
	ResponseEntity<List<PetResponse>> findOwnerPets(@PathVariable Integer ownerId) {
		List<PetResponse> response = this.service.findOwnerPets(ownerId)
			.stream()
			.map(pet -> ApiMapper.pet(pet, ownerId))
			.toList();
		return ApiResponses.cacheable(response);
	}

	@GetMapping("/pets/{petId}")
	ResponseEntity<PetResponse> findPet(@PathVariable Integer petId) {
		Owner owner = this.service.getPetOwner(petId);
		return ApiResponses.cacheable(ApiMapper.pet(this.service.getPet(petId), owner.getId()));
	}

	@PostMapping("/owners/{ownerId}/pets")
	ResponseEntity<PetResponse> createPet(@PathVariable Integer ownerId, @Valid @RequestBody PetRequest request) {
		PetResponse response = ApiMapper.pet(this.service.createPet(ownerId, request), ownerId);
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
			.path("/api/v1/pets/{id}")
			.buildAndExpand(response.id())
			.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@PutMapping("/pets/{petId}")
	PetResponse updatePet(@PathVariable Integer petId, @Valid @RequestBody PetRequest request) {
		Owner owner = this.service.getPetOwner(petId);
		return ApiMapper.pet(this.service.updatePet(petId, request), owner.getId());
	}

}
