package org.springframework.samples.petclinic.api;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record ApiPage<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public static <S, T> ApiPage<T> from(Page<S> source, Function<S, T> mapper) {
		return new ApiPage<>(source.getContent().stream().map(mapper).toList(), source.getNumber(), source.getSize(),
				source.getTotalElements(), source.getTotalPages());
	}

}
