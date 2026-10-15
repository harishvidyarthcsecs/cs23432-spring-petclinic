package org.springframework.samples.petclinic.api;

import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

final class ApiResponses {

	private ApiResponses() {
	}

	static <T> ResponseEntity<T> cacheable(T body) {
		String etag = '"' + Integer.toUnsignedString(body.hashCode(), 16) + '"';
		return ResponseEntity.ok()
			.cacheControl(CacheControl.maxAge(Duration.ofSeconds(30)).cachePublic())
			.eTag(etag)
			.body(body);
	}

}
