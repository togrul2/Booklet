package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.book.BookDto;
import com.github.togrul2.booklet.dtos.book.CreateBookDto;
import com.github.togrul2.booklet.dtos.book.UpdateBookDto;
import com.github.togrul2.booklet.entities.*;
import com.github.togrul2.booklet.repositories.AuthorRepository;
import com.github.togrul2.booklet.repositories.BookRepository;
import com.github.togrul2.booklet.repositories.GenreRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerTests {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private GenreRepository genreRepository;
    @LocalServerPort
    private int port;
    private String domain;
    private Book book;

    @BeforeEach
    public void setUp() {
        domain = "http://localhost:" + port;
        Author author = authorRepository.save(
                Author.builder()
                        .name("John")
                        .surname("Doe")
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .biography("Lorem ipsum dolor sit amet")
                        .build()
        );
        Genre genre = genreRepository.save(
                Genre.builder()
                        .name("Fantasy")
                        .slug("fantasy")
                        .build()
        );
        book = Book.builder()
                .title("Book 1")
                .author(author)
                .genre(genre)
                .isbn("1234567890")
                .build();
        Book anotherBook = Book.builder()
                .title("Book 2")
                .author(author)
                .genre(genre)
                .isbn("1234567891")
                .build();
        bookRepository.saveAll(List.of(book, anotherBook));
    }

    @AfterEach
    public void tearDown() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();
    }

    @Test
    @Disabled
    @WithAnonymousUser
    public void testGetBooks() {
        ResponseEntity<PageImpl> response = restTemplate.getForEntity(domain + "/api/v1/books", PageImpl.class);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    @WithAnonymousUser
    public void testGetBookById() {
        ResponseEntity<BookDto> response = restTemplate.getForEntity(
                domain + "/api/v1/books/{id}", BookDto.class, book.getId()
        );
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testCreateBook() {
        CreateBookDto bookDto = CreateBookDto.builder()
                .title("Book 3")
                .authorId(book.getAuthor().getId())
                .genreId(book.getGenre().getId())
                .isbn("1234567892")
                .build();
        ResponseEntity<BookDto> response = restTemplate
                .postForEntity(domain + "/api/v1/books", bookDto, BookDto.class);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getHeaders().getLocation());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testReplaceBook() {
        CreateBookDto requestBody = CreateBookDto.builder()
                .title("Book 2 updated")
                .authorId(book.getAuthor().getId())
                .genreId(book.getGenre().getId())
                .isbn("1234567892")
                .build();
        restTemplate.put(domain + "/api/v1/books/{id}", requestBody, book.getId());
        bookRepository
                .findById(book.getId())
                .ifPresent(book1 -> Assertions.assertEquals("Book 2 updated", book1.getTitle()));
    }

    @Test
    public void testUpdateBook() {
        UpdateBookDto requestBody = UpdateBookDto.builder()
                .title("Book 2 updated")
                .build();
        restTemplate.patchForObject(domain + "/api/v1/books/{id}", requestBody, BookDto.class, book.getId());
        bookRepository
                .findById(book.getId())
                .ifPresent(book1 -> Assertions.assertEquals("Book 2 updated", book1.getTitle()));
    }

    @Test
    public void testDeleteBook() {
        restTemplate.delete(domain + "/api/v1/books/{id}", book.getId());
        Assertions.assertFalse(bookRepository.existsById(book.getId()));
    }
}
