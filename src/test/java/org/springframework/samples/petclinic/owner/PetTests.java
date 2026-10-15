/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the pet age calculation shown on the owner details page.
 */
class PetTests {

	private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

	private static Pet petBornOn(LocalDate birthDate) {
		Pet pet = new Pet();
		pet.setBirthDate(birthDate);
		return pet;
	}

	@Test
	void ageIsWholeYearsSinceBirth() {
		assertEquals(4, petBornOn(LocalDate.of(2022, 3, 1)).getAgeInYears(TODAY));
	}

	@Test
	void ageDoesNotCountAnUnfinishedYear() {
		assertEquals(3, petBornOn(LocalDate.of(2022, 10, 16)).getAgeInYears(TODAY));
	}

	@Test
	void ageIsCountedOnTheBirthday() {
		assertEquals(4, petBornOn(LocalDate.of(2022, 10, 15)).getAgeInYears(TODAY));
	}

	@Test
	void newbornHasAgeZero() {
		assertEquals(0, petBornOn(TODAY).getAgeInYears(TODAY));
	}

	@Test
	void ageIsNullWhenBirthDateUnknown() {
		assertNull(petBornOn(null).getAgeInYears(TODAY));
	}

	@Test
	void ageIsNullWhenBirthDateIsInTheFuture() {
		assertNull(petBornOn(TODAY.plusDays(1)).getAgeInYears(TODAY));
	}

}
