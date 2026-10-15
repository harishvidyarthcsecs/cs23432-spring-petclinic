package org.springframework.samples.petclinic.api;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice(basePackages = "org.springframework.samples.petclinic.api")
class ApiExceptionHandler {

	@ExceptionHandler(ApiException.class)
	ProblemDetail handleApiException(ApiException exception, HttpServletRequest request) {
		return problem(exception.getStatus(), exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult()
			.getFieldErrors()
			.forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Request validation failed", request);
		problem.setProperty("fieldErrors", fieldErrors);
		return problem;
	}

	@ExceptionHandler(BindException.class)
	ProblemDetail handleBinding(BindException exception, HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getFieldErrors()
			.forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Request parameters are invalid", request);
		problem.setProperty("fieldErrors", fieldErrors);
		return problem;
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class, ConstraintViolationException.class,
			IllegalArgumentException.class })
	ProblemDetail handleBadRequest(Exception exception, HttpServletRequest request) {
		return problem(HttpStatus.BAD_REQUEST, "The request is malformed or contains invalid parameters", request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handleConflict(DataIntegrityViolationException exception, HttpServletRequest request) {
		return problem(HttpStatus.CONFLICT, "The request conflicts with existing data", request);
	}

	private ProblemDetail problem(HttpStatus status, String detail, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(status.getReasonPhrase());
		problem.setType(URI.create("urn:petclinic:problem:" + status.value()));
		problem.setInstance(URI.create(request.getRequestURI()));
		String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
		if (correlationId != null) {
			problem.setProperty("correlationId", correlationId);
		}
		return problem;
	}

}
