package com.github.togrul2.booklet.dtos.author;

import com.github.togrul2.booklet.entities.Author;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Builder
public record CreateAuthorDto(
        @NotNull
        @NotBlank
        @Length(max = Author.MAX_NAME_LENGTH)
        String name,
        @NotNull
        @NotBlank
        @Length(max = Author.MAX_SURNAME_LENGTH)
        String surname,
        @NotNull
        LocalDate birthDate,
        LocalDate deathDate,
        @NotNull
        String biography
) {
}
