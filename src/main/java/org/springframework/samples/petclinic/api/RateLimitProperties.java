package org.springframework.samples.petclinic.api;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("petclinic.ratelimit")
class RateLimitProperties {

	private boolean enabled = true;

	private int requestsPerWindow = 60;

	private Duration window = Duration.ofMinutes(1);

	private int maxClients = 10_000;

	boolean isEnabled() {
		return this.enabled;
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	int getRequestsPerWindow() {
		return this.requestsPerWindow;
	}

	void setRequestsPerWindow(int requestsPerWindow) {
		this.requestsPerWindow = requestsPerWindow;
	}

	Duration getWindow() {
		return this.window;
	}

	void setWindow(Duration window) {
		this.window = window;
	}

	int getMaxClients() {
		return this.maxClients;
	}

	void setMaxClients(int maxClients) {
		this.maxClients = maxClients;
	}

}
