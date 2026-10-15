package org.springframework.samples.petclinic.api;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "petclinic.ratelimit.enabled=false")
@AutoConfigureMockMvc
@Transactional
class ApiControllerTests {

	private static final String OWNER_JSON = """
			{"firstName":"Jamie","lastName":"Lee","address":"10 Main St",\
			"city":"Madison","telephone":"6085550100"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listsOwnersWithFilterPaginationAndSorting() throws Exception {
		this.mockMvc
			.perform(get("/api/v1/owners").param("lastName", "Davis")
				.param("page", "0")
				.param("size", "1")
				.param("sort", "firstName,desc"))
			.andExpect(status().isOk())
			.andExpect(header().exists("ETag"))
			.andExpect(header().string("Cache-Control", containsString("max-age=30")))
			.andExpect(jsonPath("$.content", hasSize(1)))
			.andExpect(jsonPath("$.content[0].firstName").value("Harold"))
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.size").value(1))
			.andExpect(jsonPath("$.totalElements").value(2))
			.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	void getsOwnerById() throws Exception {
		this.mockMvc.perform(get("/api/v1/owners/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.firstName").value("George"))
			.andExpect(jsonPath("$.pets[0].name").value("Leo"))
			.andExpect(jsonPath("$.pets[0].ageInYears", greaterThan(0)));
	}

	@Test
	void createsOwner() throws Exception {
		this.mockMvc.perform(post("/api/v1/owners").contentType(MediaType.APPLICATION_JSON).content(OWNER_JSON))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/owners/11")))
			.andExpect(jsonPath("$.id").value(11))
			.andExpect(jsonPath("$.lastName").value("Lee"));
	}

	@Test
	void updatesOwner() throws Exception {
		this.mockMvc.perform(put("/api/v1/owners/9").contentType(MediaType.APPLICATION_JSON).content(OWNER_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(9))
			.andExpect(jsonPath("$.firstName").value("Jamie"));
	}

	@Test
	void deletesOwner() throws Exception {
		this.mockMvc.perform(delete("/api/v1/owners/10")).andExpect(status().isNoContent());
		this.mockMvc.perform(get("/api/v1/owners/10")).andExpect(status().isNotFound());
	}

	@Test
	void listsPetsForOwner() throws Exception {
		this.mockMvc.perform(get("/api/v1/owners/6/pets"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].ownerId").value(6));
	}

	@Test
	void getsPetById() throws Exception {
		this.mockMvc.perform(get("/api/v1/pets/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Leo"))
			.andExpect(jsonPath("$.type.name").value("cat"));
	}

	@Test
	void createsPetUnderOwner() throws Exception {
		this.mockMvc
			.perform(post("/api/v1/owners/1/pets").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Milo\",\"birthDate\":\"2022-05-10\",\"typeId\":2}"))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/pets/")))
			.andExpect(jsonPath("$.name").value("Milo"))
			.andExpect(jsonPath("$.ownerId").value(1));
	}

	@Test
	void updatesPet() throws Exception {
		this.mockMvc
			.perform(put("/api/v1/pets/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Leo II\",\"birthDate\":\"2010-09-07\",\"typeId\":2}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.name").value("Leo II"))
			.andExpect(jsonPath("$.type.name").value("dog"));
	}

	@Test
	void listsVisitsForPet() throws Exception {
		this.mockMvc.perform(get("/api/v1/pets/8/visits"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].petId").value(8));
	}

	@Test
	void createsVisit() throws Exception {
		this.mockMvc
			.perform(post("/api/v1/pets/1/visits").contentType(MediaType.APPLICATION_JSON)
				.content("{\"date\":\"2026-10-20\",\"description\":\"Annual checkup\"}"))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/pets/1/visits/")))
			.andExpect(jsonPath("$.petId").value(1))
			.andExpect(jsonPath("$.description").value("Annual checkup"));
	}

	@Test
	void listsVetsWithPaginationAndSorting() throws Exception {
		this.mockMvc.perform(get("/api/v1/vets").param("page", "0").param("size", "2").param("sort", "lastName,asc"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", hasSize(2)))
			.andExpect(jsonPath("$.size").value(2))
			.andExpect(jsonPath("$.totalElements").value(6));
	}

	@Test
	void getsVetById() throws Exception {
		this.mockMvc.perform(get("/api/v1/vets/2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.firstName").value("Helen"))
			.andExpect(jsonPath("$.specialties[0].name").value("radiology"));
	}

	@Test
	void listsPetTypes() throws Exception {
		this.mockMvc.perform(get("/api/v1/pet-types"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(6)))
			.andExpect(jsonPath("$[0].name").value("bird"));
	}

	@Test
	void returnsProblemDetailForMissingResource() throws Exception {
		this.mockMvc.perform(get("/api/v1/owners/9999"))
			.andExpect(status().isNotFound())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.type").value("urn:petclinic:problem:404"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.instance").value("/api/v1/owners/9999"));
	}

	@Test
	void returnsNotFoundForMissingNestedResources() throws Exception {
		this.mockMvc.perform(get("/api/v1/pets/9999")).andExpect(status().isNotFound());
		this.mockMvc.perform(get("/api/v1/pets/9999/visits")).andExpect(status().isNotFound());
		this.mockMvc.perform(get("/api/v1/vets/9999")).andExpect(status().isNotFound());
	}

	@Test
	void returnsFieldErrorsForInvalidOwner() throws Exception {
		this.mockMvc.perform(post("/api/v1/owners").contentType(MediaType.APPLICATION_JSON)
			.content(
					"{\"firstName\":\"\",\"lastName\":\"Lee\",\"address\":\"\",\"city\":\"Madison\",\"telephone\":\"123\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.fieldErrors.firstName").exists())
			.andExpect(jsonPath("$.fieldErrors.address").exists())
			.andExpect(jsonPath("$.fieldErrors.telephone").exists());
	}

	@Test
	void validatesPetAndVisitRequests() throws Exception {
		this.mockMvc
			.perform(post("/api/v1/owners/1/pets").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"\",\"birthDate\":null,\"typeId\":null}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors.name").exists())
			.andExpect(jsonPath("$.fieldErrors.birthDate").exists())
			.andExpect(jsonPath("$.fieldErrors.typeId").exists());
		this.mockMvc
			.perform(post("/api/v1/pets/1/visits").contentType(MediaType.APPLICATION_JSON)
				.content("{\"date\":null,\"description\":\"\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors.date").exists())
			.andExpect(jsonPath("$.fieldErrors.description").exists());
	}

	@Test
	void rejectsInvalidPaginationAndSorting() throws Exception {
		this.mockMvc.perform(get("/api/v1/owners").param("size", "101")).andExpect(status().isBadRequest());
		this.mockMvc.perform(get("/api/v1/owners").param("page", "not-a-number")).andExpect(status().isBadRequest());
		this.mockMvc.perform(get("/api/v1/vets").param("sort", "unknown,asc"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void rejectsDuplicatePetNameAsUnprocessable() throws Exception {
		this.mockMvc
			.perform(post("/api/v1/owners/6/pets").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Max\",\"birthDate\":\"2020-01-01\",\"typeId\":1}"))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.status").value(422));
	}

}
