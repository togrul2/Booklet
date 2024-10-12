package com.github.togrul2.booklet.dtos;

import com.github.togrul2.booklet.entities.Genre;
import org.hibernate.validator.constraints.Length;

public record UpdateGenreDto(
        @Length(max = Genre.NAME_MAX_LENGTH)
        String name,
        @Length(max = Genre.NAME_MAX_LENGTH)
        String slug
) {
}
