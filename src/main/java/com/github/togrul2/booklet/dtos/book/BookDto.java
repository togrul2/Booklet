package com.github.togrul2.booklet.dtos.book;

import com.github.togrul2.booklet.entities.Author;
import com.github.togrul2.booklet.entities.Genre;

public record BookDto(
        long id,
        String title,
        Author author,
        Genre genre,
        String isbn,
        int year
) {
}