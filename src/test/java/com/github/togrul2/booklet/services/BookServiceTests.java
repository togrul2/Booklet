package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.book.BookDto;
import com.github.togrul2.booklet.dtos.book.BookFilterDto;
import com.github.togrul2.booklet.dtos.book.CreateBookDto;
import com.github.togrul2.booklet.dtos.book.UpdateBookDto;
import com.github.togrul2.booklet.entities.Author;
import com.github.togrul2.booklet.entities.Book;
import com.github.togrul2.booklet.entities.Genre;
import com.github.togrul2.booklet.mappers.BookMapper;
import com.github.togrul2.booklet.repositories.AuthorRepository;
import com.github.togrul2.booklet.repositories.BookRepository;
import com.github.togrul2.booklet.repositories.GenreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@ExtendWith(SpringExtension.class)
public class BookServiceTests {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private GenreRepository genreRepository;
    @InjectMocks
    private BookService bookService;

    private List<Book> books;
    private Book book, anotherBook;

    @BeforeEach
    public void setUp() {
        Genre genre = new Genre(1L, "Test genre", "test-genre");
        Author author = Author
                .builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .biography("Test biography")
                .build();
        this.book = Book
                .builder()
                .id(1L)
                .title("Test title")
                .author(author)
                .genre(genre)
                .isbn("1234567890")
                .year(2019)
                .build();
        this.anotherBook = Book
                .builder()
                .id(2L)
                .title("Another test title")
                .author(author)
                .genre(genre)
                .isbn("0987654321")
                .year(2020)
                .build();
        books = List.of(book);
    }

    @Test
    public void testGetAllBooks() {
        // Mock the method findAll() of bookRepository to return a Page of books.
        Mockito
                .when(bookRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(books));
        BookFilterDto bookFilterDto = BookFilterDto.builder().title("War and peace").build();
        Page<BookDto> result = bookService.findAll(PageRequest.of(0, 10), bookFilterDto);
        // Verify that the method findAll() of bookRepository was called once.
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .findAll(PageRequest.of(0, 10));
    }

    @Test
    public void testFindOneById() {
        Mockito
                .when(bookRepository.findById(book.getId()))
                .thenReturn(Optional.of(book));
        BookDto result = bookService.findById(book.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(book.getId(), result.id());
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .findById(book.getId());
    }

    @Test
    public void testFindOneByIdNotFound() {
        Mockito
                .when(bookRepository.findById(book.getId()))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> bookService.findById(book.getId()));
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .findById(book.getId());
    }

    @Test
    public void testCreate() {
        Mockito
                .when(bookRepository.save(Mockito.any(Book.class)))
                .thenReturn(book);
        Mockito
                .when(authorRepository.findById(book.getAuthor().getId()))
                .thenReturn(Optional.of(book.getAuthor()));
        Mockito
                .when(genreRepository.findById(book.getGenre().getId()))
                .thenReturn(Optional.of(book.getGenre()));

        BookDto result = bookService.create(
                CreateBookDto
                        .builder()
                        .title("Test title")
                        .authorId(book.getAuthor().getId())
                        .genreId(book.getGenre().getId())
                        .isbn("1234567890")
                        .year(2019)
                        .build()
        );
        Assertions.assertNotNull(result);
        Assertions.assertEquals(book.getId(), result.id());
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .save(Mockito.any(Book.class));
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .findById(book.getAuthor().getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(book.getGenre().getId());
    }

    @Test
    public void testCreateWithTakenIsbn() {
        Mockito
                .when(authorRepository.findById(book.getAuthor().getId()))
                .thenReturn(Optional.of(book.getAuthor()));
        Mockito
                .when(genreRepository.findById(book.getGenre().getId()))
                .thenReturn(Optional.of(book.getGenre()));
        Mockito
                .when(bookRepository.findByIsbn(book.getIsbn()))
                .thenReturn(Optional.of(anotherBook));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bookService.create(
                        CreateBookDto
                                .builder()
                                .title("Test title")
                                .authorId(book.getAuthor().getId())
                                .genreId(book.getGenre().getId())
                                .isbn(book.getIsbn())
                                .build()
                )
        );
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .findById(book.getAuthor().getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(book.getGenre().getId());
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .findByIsbn(book.getIsbn());
    }

    @Test
    public void testReplace() {
        CreateBookDto createBookDto = CreateBookDto
                .builder()
                .title("Test title updated")
                .authorId(book.getAuthor().getId())
                .genreId(book.getGenre().getId())
                .isbn("1234567890")
                .year(2020)
                .build();

        Book returnBook = BookMapper.INSTANCE.toBook(createBookDto);
        // Set the id of the returnBook.
        returnBook.setId(book.getId());

        // Mock repo calls.
        Mockito
                .when(bookRepository.existsById(book.getId()))
                .thenReturn(true);
        Mockito
                .when(authorRepository.findById(book.getAuthor().getId()))
                .thenReturn(Optional.of(book.getAuthor()));
        Mockito
                .when(genreRepository.findById(book.getGenre().getId()))
                .thenReturn(Optional.of(book.getGenre()));
        Mockito
                .when(bookRepository.save(Mockito.any(Book.class)))
                .thenReturn(returnBook);

        BookDto bookDto = bookService.replace(book.getId(), createBookDto);

        Assertions.assertEquals(book.getId(), bookDto.id());
        Assertions.assertEquals("Test title updated", bookDto.title());
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .existsById(book.getId());
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .findById(book.getAuthor().getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(book.getGenre().getId());
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .save(Mockito.any(Book.class));
    }

    @Test
    public void testReplaceNotFound() {
        CreateBookDto createBookDto = CreateBookDto
                .builder()
                .title("Test title updated")
                .authorId(book.getAuthor().getId())
                .genreId(book.getGenre().getId())
                .isbn("1234567890")
                .year(2020)
                .build();

        Mockito
                .when(bookRepository.existsById(book.getId()))
                .thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> bookService.replace(book.getId(), createBookDto));
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .existsById(book.getId());
    }

    @Test
    public void testReplaceWithTakenIsbn() {
        CreateBookDto createBookDto = CreateBookDto
                .builder()
                .title("Test title updated")
                .authorId(book.getAuthor().getId())
                .genreId(book.getGenre().getId())
                .isbn("1234567890")
                .year(2020)
                .build();

        Mockito
                .when(bookRepository.existsById(book.getId()))
                .thenReturn(true);
        Mockito
                .when(authorRepository.findById(book.getAuthor().getId()))
                .thenReturn(Optional.of(book.getAuthor()));
        Mockito
                .when(genreRepository.findById(book.getGenre().getId()))
                .thenReturn(Optional.of(book.getGenre()));
        Mockito
                .when(bookRepository.findByIsbn(createBookDto.isbn()))
                .thenReturn(Optional.of(anotherBook));

        Assertions.assertThrows(
                IllegalArgumentException.class, () -> bookService.replace(book.getId(), createBookDto)
        );
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .existsById(book.getId());
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .findById(book.getAuthor().getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(book.getGenre().getId());
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .findByIsbn(createBookDto.isbn());
    }

    @Test
    public void testUpdate() {
        Mockito
                .when(bookRepository.findById(book.getId()))
                .thenReturn(Optional.of(book));
        Mockito
                .when(bookRepository.save(Mockito.any(Book.class)))
                .thenReturn(book);
        Mockito
                .when(authorRepository.findById(book.getAuthor().getId()))
                .thenReturn(Optional.of(book.getAuthor()));
        Mockito
                .when(genreRepository.findById(book.getGenre().getId()))
                .thenReturn(Optional.of(book.getGenre()));

        BookDto updateResult = bookService.update(
                book.getId(),
                UpdateBookDto
                        .builder()
                        .title("Test title updated")
                        .authorId(book.getAuthor().getId())
                        .genreId(book.getGenre().getId())
                        .isbn("1234567890")
                        .year(2020)
                        .build()
        );

        Assertions.assertEquals(book.getId(), updateResult.id());
        Assertions.assertEquals("Test title updated", updateResult.title());
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .findById(book.getId());
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .findById(book.getAuthor().getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(book.getGenre().getId());
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .save(Mockito.any(Book.class));
    }

    @Test
    public void testUpdateWithTakenIsbn() {
        Mockito
                .when(bookRepository.findById(book.getId()))
                .thenReturn(Optional.of(book));
        Mockito
                .when(bookRepository.findByIsbn(book.getIsbn()))
                .thenReturn(Optional.of(anotherBook));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bookService.update(
                        book.getId(),
                        UpdateBookDto.builder().isbn("1234567890").build()
                )
        );
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .findById(book.getId());
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .findByIsbn(book.getIsbn());
    }

    @Test
    public void testDelete() {
        Mockito
                .doNothing()
                .when(bookRepository)
                .deleteById(book.getId());
        Assertions.assertDoesNotThrow(() -> bookService.delete(book.getId()));
        Mockito
                .verify(bookRepository, Mockito.times(1))
                .deleteById(book.getId());
    }
}
