package com.github.togrul2.booklet.dtos.book;

import com.github.togrul2.booklet.entities.Book;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UpdateBookDto (
        @NotBlank
        @Length(max = Book.TITLE_MAX_LENGTH)
        String title,
        Long authorId,
        Long genreId,
        @NotBlank
        @Length(min = Book.ISBN_MIN_LENGTH, max = Book.ISBN_MAX_LENGTH)
        String isbn,
        Integer year
) {}
