package com.github.togrul2.booklet.dtos.book;

import jakarta.validation.constraints.Positive;

public record BookFilterDto(
        String title,
        @Positive
        Long authorId,
        @Positive
        Long genreId,
        String isbn,
        Integer minYear,
        Integer maxYear
) {
}
