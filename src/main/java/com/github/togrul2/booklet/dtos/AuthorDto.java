package com.github.togrul2.booklet.dtos;

import java.time.LocalDate;

public record AuthorDto(
        long id,
        String name,
        String surname,
        LocalDate birthDate,
        LocalDate deathDate,
        String biography
) {
}
