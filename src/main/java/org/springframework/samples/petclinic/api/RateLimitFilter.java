package org.springframework.samples.petclinic.api;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
class RateLimitFilter extends OncePerRequestFilter {

	private final RateLimitProperties properties;

	private final Map<String, ClientWindow> clients = new ConcurrentHashMap<>();

	RateLimitFilter(ObjectProvider<RateLimitProperties> properties) {
		this.properties = properties.getIfAvailable(RateLimitProperties::new);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !this.properties.isEnabled() || !request.getRequestURI().startsWith("/api/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		long now = System.currentTimeMillis();
		int limit = Math.max(1, this.properties.getRequestsPerWindow());
		long windowMillis = Math.max(1_000, this.properties.getWindow().toMillis());
		ensureCapacity(now);
		ClientWindow window = this.clients.compute(request.getRemoteAddr(), (key,
				current) -> current == null || current.resetAt <= now ? new ClientWindow(now + windowMillis) : current);
		int used = window.increment();
		int remaining = Math.max(0, limit - used);
		long retryAfter = Math.max(1, (window.resetAt - now + 999) / 1_000);
		response.setHeader("X-RateLimit-Limit", Integer.toString(limit));
		response.setHeader("X-RateLimit-Remaining", Integer.toString(remaining));
		response.setHeader("X-RateLimit-Reset", Long.toString(Instant.ofEpochMilli(window.resetAt).getEpochSecond()));
		if (used > limit) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
			response.setHeader("Retry-After", Long.toString(retryAfter));
			response.getWriter()
				.write("{\"type\":\"urn:petclinic:problem:429\",\"title\":\"Too Many Requests\","
						+ "\"status\":429,\"detail\":\"API rate limit exceeded\"}");
			return;
		}
		filterChain.doFilter(request, response);
	}

	private void ensureCapacity(long now) {
		int maxClients = Math.max(1, this.properties.getMaxClients());
		if (this.clients.size() < maxClients) {
			return;
		}
		this.clients.entrySet().removeIf(entry -> entry.getValue().resetAt <= now);
		if (this.clients.size() >= maxClients) {
			this.clients.keySet().stream().findFirst().ifPresent(this.clients::remove);
		}
	}

	private static final class ClientWindow {

		private final long resetAt;

		private int requests;

		private ClientWindow(long resetAt) {
			this.resetAt = resetAt;
		}

		private synchronized int increment() {
			return ++this.requests;
		}

	}

}
