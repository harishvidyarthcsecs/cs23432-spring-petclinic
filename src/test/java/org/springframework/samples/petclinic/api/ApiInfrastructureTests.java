package org.springframework.samples.petclinic.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

@SpringBootTest(properties = { "petclinic.ratelimit.enabled=true", "petclinic.ratelimit.requests-per-window=2",
		"petclinic.ratelimit.window=10m" })
@AutoConfigureMockMvc
class ApiInfrastructureTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void preservesOrCreatesCorrelationId() throws Exception {
		this.mockMvc.perform(get("/").header("X-Correlation-Id", "client-request-123"))
			.andExpect(status().isOk())
			.andExpect(header().string("X-Correlation-Id", "client-request-123"));
		this.mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(header().exists("X-Correlation-Id"));
	}

	@Test
	void addsSecurityHeadersAndRelaxesDocumentationCsp() throws Exception {
		this.mockMvc.perform(get("/"))
			.andExpect(status().isOk())
			.andExpect(header().string("X-Content-Type-Options", "nosniff"))
			.andExpect(header().string("X-Frame-Options", "DENY"))
			.andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
			.andExpect(header().string("Permissions-Policy", containsString("camera=()")))
			.andExpect(header().string("Content-Security-Policy",
					org.hamcrest.Matchers.not(containsString("unsafe-inline"))));
		this.mockMvc.perform(get("/swagger-ui.html"))
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string("Content-Security-Policy", containsString("unsafe-inline")));
	}

	@Test
	void rateLimitsApiByClientAddress() throws Exception {
		this.mockMvc.perform(get("/api/v1/pet-types").with(request -> remote(request, "198.51.100.10")))
			.andExpect(status().isOk())
			.andExpect(header().string("X-RateLimit-Limit", "2"))
			.andExpect(header().string("X-RateLimit-Remaining", "1"));
		this.mockMvc.perform(get("/api/v1/pet-types").with(request -> remote(request, "198.51.100.10")))
			.andExpect(status().isOk())
			.andExpect(header().string("X-RateLimit-Remaining", "0"));
		this.mockMvc.perform(get("/api/v1/pet-types").with(request -> remote(request, "198.51.100.10")))
			.andExpect(status().isTooManyRequests())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(header().exists("Retry-After"))
			.andExpect(jsonPath("$.status").value(429));
	}

	@Test
	void exposesHealthAndPrometheus() throws Exception {
		this.mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
		this.mockMvc.perform(get("/actuator/health/liveness"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
		this.mockMvc.perform(get("/actuator/health/readiness"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
		this.mockMvc.perform(get("/actuator/prometheus"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("jvm_")));
	}

	@Test
	void exposesOpenApiDocumentWithMetadataAndRoutes() throws Exception {
		this.mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.info.title").value("Spring PetClinic API"))
			.andExpect(jsonPath("$.info.version").value("v1"))
			.andExpect(jsonPath("$.paths['/api/v1/owners']").exists())
			.andExpect(jsonPath("$.paths['/api/v1/pet-types']").exists());
	}

	private org.springframework.mock.web.MockHttpServletRequest remote(
			org.springframework.mock.web.MockHttpServletRequest request, String address) {
		request.setRemoteAddr(address);
		return request;
	}

}
