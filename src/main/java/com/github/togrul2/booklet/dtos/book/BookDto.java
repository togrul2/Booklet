package com.github.togrul2.booklet.dtos.book;

import com.github.togrul2.booklet.dtos.author.AuthorDto;
import com.github.togrul2.booklet.dtos.genre.GenreDto;

public record BookDto(
        long id,
        String title,
        AuthorDto author,
        GenreDto genre,
        String isbn,
        int year
) {
}