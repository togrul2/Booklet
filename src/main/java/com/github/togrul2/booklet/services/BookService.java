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
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;

    public Page<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).map(BookMapper.INSTANCE::toBookDto);
    }

    public BookDto findOneById(long id) {
        Book book = bookRepository.findById(id).orElseThrow(BookNotFound::new);
        return BookMapper.INSTANCE.toBookDto(book);
    }

    private void checkIsbnAvailability(String isbn) {
        if (bookRepository.existsByIsbn(isbn)) {
            throw new IllegalArgumentException("ISBN is already taken");
        }
    }

    /**
     * Checks if given isbn is taken by someone with different id than target one.
     *
     * @param isbn isbn to check uniqueness.
     * @param id   ID of the book to exclude from checking.
     */
    private void checkIsbnAvailability(String isbn, long id) {
        if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new IllegalArgumentException("ISBN is already taken");
        }
    }

    public BookDto create(@NonNull CreateBookDto createBookDto) {
        checkIsbnAvailability(createBookDto.isbn());
        Book book = BookMapper.INSTANCE.toBook(createBookDto);
        book.setAuthor(authorRepository.findById(createBookDto.authorId()).orElseThrow(AuthorNotFound::new));
        book.setGenre(genreRepository.findById(createBookDto.genreId()).orElseThrow(GenreNotFound::new));
        return BookMapper.INSTANCE.toBookDto(bookRepository.save(book));
    }

    public BookDto replace(long id, @NonNull CreateBookDto createBookDto) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFound();
        }

        checkIsbnAvailability(createBookDto.isbn(), id);
        Book book = BookMapper.INSTANCE.toBook(createBookDto);
        book.setId(id);
        book.setAuthor(authorRepository.findById(createBookDto.authorId()).orElseThrow(AuthorNotFound::new));
        book.setGenre(genreRepository.findById(createBookDto.genreId()).orElseThrow(GenreNotFound::new));
        return BookMapper.INSTANCE.toBookDto(bookRepository.save(book));
    }

    public BookDto update(long id, @NonNull UpdateBookDto updateBookDto) {
        Book book = bookRepository.findById(id).orElseThrow(BookNotFound::new);
        checkIsbnAvailability(updateBookDto.isbn(), id);

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

        return BookMapper.INSTANCE.toBookDto(bookRepository.save(book));
    }

    public void delete(long id) {
        bookRepository.deleteById(id);
    }
}
