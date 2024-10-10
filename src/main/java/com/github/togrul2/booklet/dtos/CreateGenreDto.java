package com.github.togrul2.booklet.dtos;

import com.github.togrul2.booklet.entities.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CreateGenreDto(
        @NotNull
        @NotBlank
        @Length(max = Genre.NAME_MAX_LENGTH)
        String name,
        @NotNull
        @NotBlank
        @Length(max = Genre.NAME_MAX_LENGTH)
        String slug
) {
}
