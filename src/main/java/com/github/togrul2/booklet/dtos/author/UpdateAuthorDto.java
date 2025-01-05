package com.github.togrul2.booklet.dtos.author;

import com.github.togrul2.booklet.entities.Author;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Builder
public record UpdateAuthorDto(
        @Length(max = Author.MAX_NAME_LENGTH)
        String name,
        @Length(max = Author.MAX_SURNAME_LENGTH)
        String surname,
        LocalDate birthDate,
        LocalDate deathDate,
        String biography
) {
}
