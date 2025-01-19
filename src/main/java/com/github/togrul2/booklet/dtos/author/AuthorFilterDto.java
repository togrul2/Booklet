package com.github.togrul2.booklet.dtos.author;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
public record AuthorFilterDto(
        String name,
        String surname,
        LocalDate minBirthDate,
        LocalDate maxBirthDate,
        LocalDate minDeathDate,
        LocalDate maxDeathDate
) implements Serializable {
}
