package com.github.togrul2.booklet.dtos;

import com.github.togrul2.booklet.entities.Genre;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UpdateGenreDto(
        @NotBlank
        @Length(max = Genre.NAME_MAX_LENGTH)
        String name,
        @NotBlank
        @Length(max = Genre.NAME_MAX_LENGTH)
        String slug
) {
}
