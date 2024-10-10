package com.github.togrul2.booklet.dtos;

import com.github.togrul2.booklet.entities.Book;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record CreateBookDto(
        @NotNull
        @NotBlank
        @Length(max = Book.TITLE_MAX_LENGTH)
        String title,
        @NotNull
        long authorId,
        @NotNull
        long genreId,
        @NotNull
        @NotBlank
        @Length(min = Book.ISBN_MIN_LENGTH, max = Book.ISBN_MAX_LENGTH)
        String isbn,
        @NotNull
        int year
) {
}
