package com.github.togrul2.booklet.dtos.book;

import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record BookFilterDto(
        String title,
        @Positive
        Long authorId,
        @Positive
        Long genreId,
        String isbn,
        Integer minYear,
        Integer maxYear
) implements Serializable {
}
