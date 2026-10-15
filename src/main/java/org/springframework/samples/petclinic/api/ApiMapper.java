package org.springframework.samples.petclinic.api;

import java.time.LocalDate;

import org.springframework.samples.petclinic.api.ApiDtos.OwnerResponse;
import org.springframework.samples.petclinic.api.ApiDtos.PetResponse;
import org.springframework.samples.petclinic.api.ApiDtos.PetTypeResponse;
import org.springframework.samples.petclinic.api.ApiDtos.SpecialtyResponse;
import org.springframework.samples.petclinic.api.ApiDtos.VetResponse;
import org.springframework.samples.petclinic.api.ApiDtos.VisitResponse;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.Vet;

final class ApiMapper {

	private ApiMapper() {
	}

	static OwnerResponse owner(Owner owner) {
		return new OwnerResponse(owner.getId(), owner.getFirstName(), owner.getLastName(), owner.getAddress(),
				owner.getCity(), owner.getTelephone(),
				owner.getPets().stream().map(pet -> pet(pet, owner.getId())).toList());
	}

	static PetResponse pet(Pet pet, Integer ownerId) {
		return new PetResponse(pet.getId(), ownerId, pet.getName(), pet.getBirthDate(),
				pet.getAgeInYears(LocalDate.now()), petType(pet.getType()));
	}

	static VisitResponse visit(Visit visit, Integer petId) {
		return new VisitResponse(visit.getId(), petId, visit.getDate(), visit.getDescription());
	}

	static VetResponse vet(Vet vet) {
		return new VetResponse(vet.getId(), vet.getFirstName(), vet.getLastName(),
				vet.getSpecialties()
					.stream()
					.map(specialty -> new SpecialtyResponse(specialty.getId(), specialty.getName()))
					.toList());
	}

	static PetTypeResponse petType(PetType petType) {
		return new PetTypeResponse(petType.getId(), petType.getName());
	}

}
