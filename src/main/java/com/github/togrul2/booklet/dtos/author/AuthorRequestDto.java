package com.github.togrul2.booklet.dtos.author;

import com.github.togrul2.booklet.entities.Author;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Builder
public record AuthorRequestDto(
        @NotNull(groups = CreateAuthor.class)
        @NotBlank(groups = CreateAuthor.class)
        @Length(max = Author.MAX_NAME_LENGTH, groups = {CreateAuthor.class, UpdateAuthor.class})
        String name,
        @NotNull(groups = CreateAuthor.class)
        @NotBlank(groups = CreateAuthor.class)
        @Length(max = Author.MAX_SURNAME_LENGTH, groups = {CreateAuthor.class, UpdateAuthor.class})
        String surname,
        @NotNull(groups = CreateAuthor.class)
        LocalDate birthDate,
        LocalDate deathDate,
        @NotNull(groups = CreateAuthor.class)
        String biography
) {
}
