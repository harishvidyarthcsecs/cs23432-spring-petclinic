package org.springframework.samples.petclinic.api;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.api.ApiDtos.OwnerRequest;
import org.springframework.samples.petclinic.api.ApiDtos.PetRequest;
import org.springframework.samples.petclinic.api.ApiDtos.VisitRequest;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeRepository;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ApiService {

	private static final int MAX_PAGE_SIZE = 100;

	private final OwnerRepository owners;

	private final ApiPetRepository pets;

	private final PetTypeRepository petTypes;

	private final VetRepository vets;

	ApiService(OwnerRepository owners, ApiPetRepository pets, PetTypeRepository petTypes, VetRepository vets) {
		this.owners = owners;
		this.pets = pets;
		this.petTypes = petTypes;
		this.vets = vets;
	}

	@Transactional(readOnly = true)
	Page<Owner> findOwners(String lastName, Pageable pageable) {
		validatePageable(pageable, List.of("id", "firstName", "lastName", "address", "city", "telephone"));
		return this.owners.findByLastNameStartingWith(lastName == null ? "" : lastName, pageable);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "owners", key = "#ownerId")
	public Owner getOwner(Integer ownerId) {
		return this.owners.findById(ownerId).orElseThrow(() -> ApiException.notFound("Owner", ownerId));
	}

	@Transactional
	Owner createOwner(OwnerRequest request) {
		Owner owner = new Owner();
		copyOwner(request, owner);
		return this.owners.save(owner);
	}

	@Transactional
	@CacheEvict(value = "owners", key = "#ownerId")
	Owner updateOwner(Integer ownerId, OwnerRequest request) {
		Owner owner = this.owners.findById(ownerId).orElseThrow(() -> ApiException.notFound("Owner", ownerId));
		copyOwner(request, owner);
		return this.owners.save(owner);
	}

	@Transactional
	@CacheEvict(value = "owners", key = "#ownerId")
	void deleteOwner(Integer ownerId) {
		Owner owner = this.owners.findById(ownerId).orElseThrow(() -> ApiException.notFound("Owner", ownerId));
		this.owners.delete(owner);
		this.owners.flush();
	}

	@Transactional(readOnly = true)
	List<Pet> findOwnerPets(Integer ownerId) {
		getOwner(ownerId);
		return this.pets.findByOwnerId(ownerId);
	}

	@Transactional(readOnly = true)
	Pet getPet(Integer petId) {
		return this.pets.findById(petId).orElseThrow(() -> ApiException.notFound("Pet", petId));
	}

	@Transactional(readOnly = true)
	Owner getPetOwner(Integer petId) {
		return this.pets.findOwnerByPetId(petId).orElseThrow(() -> ApiException.notFound("Pet", petId));
	}

	@Transactional
	@CacheEvict(value = "owners", key = "#ownerId")
	Pet createPet(Integer ownerId, PetRequest request) {
		Owner owner = this.owners.findById(ownerId).orElseThrow(() -> ApiException.notFound("Owner", ownerId));
		ensureUniquePetName(owner, request.name(), null);
		Pet pet = new Pet();
		copyPet(request, pet);
		owner.addPet(pet);
		this.owners.saveAndFlush(owner);
		return pet;
	}

	@Transactional
	@CacheEvict(value = "owners", allEntries = true)
	Pet updatePet(Integer petId, PetRequest request) {
		Pet pet = getPet(petId);
		Owner owner = getPetOwner(petId);
		ensureUniquePetName(owner, request.name(), petId);
		copyPet(request, pet);
		return this.pets.saveAndFlush(pet);
	}

	@Transactional(readOnly = true)
	List<Visit> findPetVisits(Integer petId) {
		return getPet(petId).getVisits().stream().toList();
	}

	@Transactional
	@CacheEvict(value = "owners", allEntries = true)
	Visit createVisit(Integer petId, VisitRequest request) {
		Pet pet = getPet(petId);
		Visit visit = new Visit();
		visit.setDate(request.date());
		visit.setDescription(request.description());
		pet.addVisit(visit);
		this.pets.saveAndFlush(pet);
		return visit;
	}

	@Transactional(readOnly = true)
	Page<Vet> findVets(Pageable pageable) {
		validatePageable(pageable, List.of("id", "firstName", "lastName"));
		return this.vets.findAll(pageable);
	}

	@Transactional(readOnly = true)
	Vet getVet(Integer vetId) {
		return this.vets.findById(vetId).orElseThrow(() -> ApiException.notFound("Veterinarian", vetId));
	}

	@Transactional(readOnly = true)
	@Cacheable("petTypes")
	List<PetType> findPetTypes() {
		return this.petTypes.findPetTypes();
	}

	private void copyOwner(OwnerRequest request, Owner owner) {
		owner.setFirstName(request.firstName());
		owner.setLastName(request.lastName());
		owner.setAddress(request.address());
		owner.setCity(request.city());
		owner.setTelephone(request.telephone());
	}

	private void copyPet(PetRequest request, Pet pet) {
		PetType type = this.petTypes.findById(request.typeId())
			.orElseThrow(() -> ApiException.unprocessable("Pet type " + request.typeId() + " does not exist"));
		pet.setName(request.name());
		pet.setBirthDate(request.birthDate());
		pet.setType(type);
	}

	private void ensureUniquePetName(Owner owner, String name, Integer currentPetId) {
		boolean duplicate = owner.getPets()
			.stream()
			.anyMatch(pet -> pet.getName().equalsIgnoreCase(name) && !Objects.equals(pet.getId(), currentPetId));
		if (duplicate) {
			throw ApiException.unprocessable("Owner already has a pet named " + name);
		}
	}

	private void validatePageable(Pageable pageable, List<String> allowedSorts) {
		if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1 || pageable.getPageSize() > MAX_PAGE_SIZE) {
			throw ApiException.badRequest("page must be non-negative and size must be between 1 and " + MAX_PAGE_SIZE);
		}
		pageable.getSort().forEach(order -> {
			if (!allowedSorts.contains(order.getProperty())) {
				throw ApiException.badRequest("Unsupported sort property: " + order.getProperty());
			}
		});
	}

}
