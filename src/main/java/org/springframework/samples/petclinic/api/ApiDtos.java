package org.springframework.samples.petclinic.api;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ApiDtos {

	private ApiDtos() {
	}

	public record OwnerRequest(@NotBlank @Size(max = 30) String firstName, @NotBlank @Size(max = 30) String lastName,
			@NotBlank @Size(max = 255) String address, @NotBlank @Size(max = 80) String city,
			@NotBlank @Pattern(regexp = "\\d{10}", message = "must contain exactly 10 digits") String telephone) {
	}

	public record OwnerResponse(Integer id, String firstName, String lastName, String address, String city,
			String telephone, List<PetResponse> pets) {
	}

	public record PetRequest(@NotBlank @Size(max = 30) String name, @NotNull LocalDate birthDate,
			@NotNull Integer typeId) {
	}

	public record PetResponse(Integer id, Integer ownerId, String name, LocalDate birthDate, Integer ageInYears,
			PetTypeResponse type) {
	}

	public record VisitRequest(@NotNull LocalDate date, @NotBlank @Size(max = 255) String description) {
	}

	public record VisitResponse(Integer id, Integer petId, LocalDate date, String description) {
	}

	public record VetResponse(Integer id, String firstName, String lastName, List<SpecialtyResponse> specialties) {
	}

	public record SpecialtyResponse(Integer id, String name) {
	}

	public record PetTypeResponse(Integer id, String name) {
	}

}
