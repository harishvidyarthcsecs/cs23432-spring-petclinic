package org.springframework.samples.petclinic.api;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class SecurityHeadersFilter extends OncePerRequestFilter {

	private static final String APPLICATION_CSP = "default-src 'self'; script-src 'self'; style-src 'self'; "
			+ "img-src 'self' data:; font-src 'self'; object-src 'none'; frame-ancestors 'none'; base-uri 'self'; "
			+ "form-action 'self'";

	private static final String DOCUMENTATION_CSP = "default-src 'self'; script-src 'self' 'unsafe-inline'; "
			+ "style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:; connect-src 'self'; "
			+ "object-src 'none'; frame-ancestors 'none'; base-uri 'self'";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		response.setHeader("X-Content-Type-Options", "nosniff");
		response.setHeader("X-Frame-Options", "DENY");
		response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
		response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
		response.setHeader("Content-Security-Policy",
				isDocumentationRequest(request) ? DOCUMENTATION_CSP : APPLICATION_CSP);
		filterChain.doFilter(request, response);
	}

	private boolean isDocumentationRequest(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
	}

}
