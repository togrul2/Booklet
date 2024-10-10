package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.BookDto;
import com.github.togrul2.booklet.dtos.CreateBookDto;
import com.github.togrul2.booklet.dtos.UpdateBookDto;
import com.github.togrul2.booklet.entities.Book;
import com.github.togrul2.booklet.exceptions.AuthorNotFound;
import com.github.togrul2.booklet.exceptions.BookNotFound;
import com.github.togrul2.booklet.exceptions.GenreNotFound;
import com.github.togrul2.booklet.mappers.BookMapper;
import com.github.togrul2.booklet.repositories.AuthorRepository;
import com.github.togrul2.booklet.repositories.BookRepository;
import com.github.togrul2.booklet.repositories.GenreRepository;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;

    public Page<BookDto> findAll(int pageNumber, int pageSize) {
        return bookRepository
                .findAll(PageRequest.of(pageNumber - 1, pageSize))
                .map(BookMapper.INSTANCE::toBookDto);
    }

    public BookDto findOneById(long id) {
        return BookMapper.INSTANCE.toBookDto(bookRepository.findById(id).orElseThrow(BookNotFound::new));
    }

    public BookDto create(CreateBookDto createBookDto) {
        // TODO: validate unique fields.
        Book book = BookMapper.INSTANCE.toBook(createBookDto);
        book.setAuthor(authorRepository.findById(createBookDto.authorId()).orElseThrow(AuthorNotFound::new));
        book.setGenre(genreRepository.findById(createBookDto.genreId()).orElseThrow(GenreNotFound::new));
        return BookMapper.INSTANCE.toBookDto(bookRepository.save(book));
    }

    public BookDto replace(long id, CreateBookDto createBookDto) {
        if (!bookRepository.existsById(id))
            throw new BookNotFound();

        Book book = BookMapper.INSTANCE.toBook(createBookDto);
        book.setId(id);
        book.setAuthor(authorRepository.findById(createBookDto.authorId()).orElseThrow(AuthorNotFound::new));
        book.setGenre(genreRepository.findById(createBookDto.genreId()).orElseThrow(GenreNotFound::new));
        return BookMapper.INSTANCE.toBookDto(bookRepository.save(book));
    }

    public BookDto update(long id, UpdateBookDto updateBookDto) {
        Book book = bookRepository.findById(id).orElseThrow(BookNotFound::new);

        if (updateBookDto.title() != null)
            book.setTitle(updateBookDto.title());
        if (updateBookDto.authorId() != null)
            book.setAuthor(authorRepository.findById(updateBookDto.authorId()).orElseThrow(AuthorNotFound::new));
        if (updateBookDto.genreId() != null)
            book.setGenre(genreRepository.findById(updateBookDto.genreId()).orElseThrow(GenreNotFound::new));
        if (updateBookDto.isbn() != null)
            book.setIsbn(updateBookDto.isbn());
        if (updateBookDto.year() != null)
            book.setYear(updateBookDto.year());

        return BookMapper.INSTANCE.toBookDto(bookRepository.save(book));
    }

    public void delete(long id) {
        bookRepository.deleteById(id);
    }
}
