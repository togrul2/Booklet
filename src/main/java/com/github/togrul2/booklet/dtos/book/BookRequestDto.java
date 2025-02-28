package com.github.togrul2.booklet.dtos.book;

import com.github.togrul2.booklet.entities.Book;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record BookRequestDto(
        @NotNull(groups = CreateBook.class)
        @NotBlank(groups = CreateBook.class)
        @Length(max = Book.TITLE_MAX_LENGTH, groups = {CreateBook.class, UpdateBook.class})
        String title,
        @NotNull(groups = CreateBook.class)
        Long authorId,
        @NotNull(groups = CreateBook.class)
        Long genreId,
        @NotNull(groups = CreateBook.class)
        @NotBlank(groups = CreateBook.class)
        @Length(min = Book.ISBN_MIN_LENGTH, max = Book.ISBN_MAX_LENGTH, groups = {CreateBook.class, UpdateBook.class})
        String isbn,
        @NotNull(groups = CreateBook.class)
        Integer year
) {
}
