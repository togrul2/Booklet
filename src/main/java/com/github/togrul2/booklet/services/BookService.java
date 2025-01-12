package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.book.BookDto;
import com.github.togrul2.booklet.dtos.book.CreateBookDto;
import com.github.togrul2.booklet.dtos.book.UpdateBookDto;
import com.github.togrul2.booklet.entities.Book;
import com.github.togrul2.booklet.exceptions.AuthorNotFound;
import com.github.togrul2.booklet.exceptions.BookNotFound;
import com.github.togrul2.booklet.exceptions.GenreNotFound;
import com.github.togrul2.booklet.mappers.BookMapper;
import com.github.togrul2.booklet.repositories.AuthorRepository;
import com.github.togrul2.booklet.repositories.BookRepository;
import com.github.togrul2.booklet.repositories.GenreRepository;
import com.github.togrul2.booklet.security.annotations.IsAdmin;
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
        Book book = bookRepository.findById(id).orElseThrow(BookNotFound::new);
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
        book.setAuthor(authorRepository.findById(createBookDto.authorId()).orElseThrow(AuthorNotFound::new));
        book.setGenre(genreRepository.findById(createBookDto.genreId()).orElseThrow(GenreNotFound::new));
        validateBook(book);
        return BookMapper.INSTANCE.toBookDto(bookRepository.save(book));
    }

    @IsAdmin
    public BookDto replace(long id, @NonNull CreateBookDto createBookDto) {
        // Check if book exists. If not throw BookNotFound exception.
        if (!bookRepository.existsById(id)) {
            throw new BookNotFound();
        }

        Book book = BookMapper.INSTANCE.toBook(createBookDto);
        book.setId(id);
        book.setAuthor(authorRepository.findById(createBookDto.authorId()).orElseThrow(AuthorNotFound::new));
        book.setGenre(genreRepository.findById(createBookDto.genreId()).orElseThrow(GenreNotFound::new));
        validateBook(book);
        return BookMapper.INSTANCE.toBookDto(bookRepository.save(book));
    }

    @IsAdmin
    public BookDto update(long id, @NonNull UpdateBookDto updateBookDto) {
        Book book = bookRepository.findById(id).orElseThrow(BookNotFound::new);

        if (updateBookDto.title() != null) {
            book.setTitle(updateBookDto.title());
        }

        if (updateBookDto.authorId() != null) {
            book.setAuthor(authorRepository.findById(updateBookDto.authorId()).orElseThrow(AuthorNotFound::new));
        }

        if (updateBookDto.genreId() != null) {
            book.setGenre(genreRepository.findById(updateBookDto.genreId()).orElseThrow(GenreNotFound::new));
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
