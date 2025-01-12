package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.book.BookDto;
import com.github.togrul2.booklet.dtos.book.CreateBookDto;
import com.github.togrul2.booklet.dtos.book.UpdateBookDto;
import com.github.togrul2.booklet.entities.Author;
import com.github.togrul2.booklet.entities.Book;
import com.github.togrul2.booklet.entities.Genre;
import com.github.togrul2.booklet.mappers.BookMapper;
import com.github.togrul2.booklet.repositories.AuthorRepository;
import com.github.togrul2.booklet.repositories.BookRepository;
import com.github.togrul2.booklet.repositories.GenreRepository;
import com.github.togrul2.booklet.security.annotations.IsAdmin;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;

    public Page<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).map(BookMapper.INSTANCE::toBookDto);
    }

    public BookDto findById(long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Book not found."));
        return BookMapper.INSTANCE.toBookDto(book);
    }

    private void validateBook(Book book) {
        bookRepository.findByIsbn(book.getIsbn()).ifPresent(b -> {
            if (b.getId() == null || !Objects.equals(b.getId(), book.getId())) {
                throw new IllegalArgumentException("Book with this ISBN already exists.");
            }
        });
    }

    @IsAdmin
    public BookDto create(@NonNull CreateBookDto createBookDto) {
        Book book = BookMapper.INSTANCE.toBook(createBookDto);
        Author author = authorRepository
                .findById(createBookDto.authorId())
                .orElseThrow(() -> new EntityNotFoundException("Author not found."));
        Genre genre = genreRepository
                .findById(createBookDto.genreId())
                .orElseThrow(() -> new EntityNotFoundException("Genre not found."));
        book.setAuthor(author);
        book.setGenre(genre);
        validateBook(book);
        return BookMapper.INSTANCE.toBookDto(bookRepository.save(book));
    }

    @IsAdmin
    public BookDto replace(long id, @NonNull CreateBookDto createBookDto) {
        // Check if book exists. If not throw BookNotFound exception.
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book not found.");
        }

        Book book = BookMapper.INSTANCE.toBook(createBookDto);
        Author author = authorRepository
                .findById(createBookDto.authorId())
                .orElseThrow(() -> new EntityNotFoundException("Author not found."));
        Genre genre = genreRepository
                .findById(createBookDto.genreId())
                .orElseThrow(() -> new EntityNotFoundException("Genre not found."));
        book.setId(id);
        book.setAuthor(author);
        book.setGenre(genre);
        validateBook(book);
        return BookMapper.INSTANCE.toBookDto(bookRepository.save(book));
    }

    @IsAdmin
    public BookDto update(long id, @NonNull UpdateBookDto updateBookDto) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Book not found."));

        if (updateBookDto.title() != null) {
            book.setTitle(updateBookDto.title());
        }

        if (updateBookDto.authorId() != null) {
            Author author = authorRepository
                    .findById(updateBookDto.authorId())
                    .orElseThrow(() -> new EntityNotFoundException("Author not found."));
            book.setAuthor(author);
        }

        if (updateBookDto.genreId() != null) {
            Genre genre = genreRepository
                    .findById(updateBookDto.genreId())
                    .orElseThrow(() -> new EntityNotFoundException("Genre not found."));
            book.setGenre(genre);
        }

        if (updateBookDto.isbn() != null) {
            book.setIsbn(updateBookDto.isbn());
        }

        if (updateBookDto.year() != null) {
            book.setYear(updateBookDto.year());
        }

        validateBook(book);
        return BookMapper.INSTANCE.toBookDto(bookRepository.save(book));
    }

    @IsAdmin
    public void delete(long id) {
        bookRepository.deleteById(id);
    }
}
