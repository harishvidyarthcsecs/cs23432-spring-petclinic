package org.springframework.samples.petclinic.api;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

	private final HttpStatus status;

	private ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public static ApiException notFound(String resource, Integer id) {
		return new ApiException(HttpStatus.NOT_FOUND, resource + " " + id + " was not found");
	}

	public static ApiException badRequest(String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, message);
	}

	public static ApiException unprocessable(String message) {
		return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, message);
	}

	public HttpStatus getStatus() {
		return this.status;
	}

}
