package org.springframework.samples.petclinic.api;

import jakarta.servlet.http.HttpServletRequest;

final class ApiPagination {

	private ApiPagination() {
	}

	static void validate(HttpServletRequest request) {
		validateInteger(request.getParameter("page"), "page", 0, Integer.MAX_VALUE);
		validateInteger(request.getParameter("size"), "size", 1, 100);
		String[] sorts = request.getParameterValues("sort");
		if (sorts == null) {
			return;
		}
		for (String sort : sorts) {
			String[] parts = sort.split(",", -1);
			if (parts.length > 2 || parts[0].isBlank()
					|| (parts.length == 2 && !parts[1].equalsIgnoreCase("asc") && !parts[1].equalsIgnoreCase("desc"))) {
				throw ApiException.badRequest("sort must use the format property,asc or property,desc");
			}
		}
	}

	private static void validateInteger(String value, String name, int minimum, int maximum) {
		if (value == null) {
			return;
		}
		try {
			int parsed = Integer.parseInt(value);
			if (parsed < minimum || parsed > maximum) {
				throw ApiException.badRequest(name + " is outside the allowed range");
			}
		}
		catch (NumberFormatException exception) {
			throw ApiException.badRequest(name + " must be an integer");
		}
	}

}
