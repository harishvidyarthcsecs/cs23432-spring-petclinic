package org.springframework.samples.petclinic.api;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter extends OncePerRequestFilter {

	static final String HEADER_NAME = "X-Correlation-Id";

	static final String MDC_KEY = "correlationId";

	private static final Pattern SAFE_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._-]{1,128}");

	private static final Logger logger = LoggerFactory.getLogger(CorrelationIdFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String correlationId = correlationId(request.getHeader(HEADER_NAME));
		long start = System.nanoTime();
		response.setHeader(HEADER_NAME, correlationId);
		try (MDC.MDCCloseable ignored = MDC.putCloseable(MDC_KEY, correlationId)) {
			try {
				filterChain.doFilter(request, response);
			}
			finally {
				long durationMillis = (System.nanoTime() - start) / 1_000_000;
				logger.info("request method={} path={} status={} durationMs={}", request.getMethod(),
						request.getRequestURI(), response.getStatus(), durationMillis);
			}
		}
	}

	private String correlationId(String supplied) {
		if (supplied != null && SAFE_CORRELATION_ID.matcher(supplied).matches()) {
			return supplied;
		}
		return UUID.randomUUID().toString();
	}

}
