package com.github.togrul2.booklet.dtos.author;

import com.github.togrul2.booklet.entities.Author;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Builder
public record UpdateAuthorDto(
        @NotBlank
        @Length(max = Author.MAX_NAME_LENGTH)
        String name,
        @NotBlank
        @Length(max = Author.MAX_SURNAME_LENGTH)
        String surname,
        LocalDate birthDate,
        LocalDate deathDate,
        String biography
) {
}
