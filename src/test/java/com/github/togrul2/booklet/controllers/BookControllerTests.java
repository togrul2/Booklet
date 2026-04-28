package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.annotations.WithMockAdminUser;
import com.github.togrul2.booklet.configurations.TestcontainersConfiguration;
import com.github.togrul2.booklet.entities.*;
import com.github.togrul2.booklet.repositories.AuthorRepository;
import com.github.togrul2.booklet.repositories.BookRepository;
import com.github.togrul2.booklet.repositories.GenreRepository;
import com.github.togrul2.booklet.services.JwtService;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

@Testcontainers
@WithMockAdminUser
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerTests {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private JwtService jwtService;
    @LocalServerPort
    private int port;

    private Book book;

    @BeforeEach
    public void setUp() {
        User authUser = User.builder().email("johndoe@example.com").role(Role.ADMIN).build();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.authentication = RestAssured.oauth2(jwtService.createAccessToken(authUser, authUser.getRole()));

        Author author = authorRepository.save(
                Author.builder()
                        .name("John")
                        .surname("Doe")
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .biography("Lorem ipsum dolor sit amet")
                        .build()
        );
        // Create a genre fails due to access restrictions.
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
    public void testGetBooks() {
        RestAssured.given()
                .when()
                .get("/api/v1/books")
                .then()
                .statusCode(200)
                .body("content.size()", Matchers.is(2));
    }

    @Test
    public void testGetBookById() {
        RestAssured.given()
                .when()
                .get("/api/v1/books/{id}", book.getId())
                .then()
                .statusCode(200)
                .body("id", Matchers.is(book.getId().intValue()));
    }

    @Test
    public void testCreateBook() {
        String requestBody = """
                {
                    "title": "Book 3",
                    "authorId": %d,
                    "genreId": %d,
                    "isbn": "1234567892",
                    "year": 2000
                }
                """.formatted(book.getAuthor().getId(), book.getGenre().getId());

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/v1/books")
                .then()
                .log()
                .ifError()
                .statusCode(201)
                .header("Location", Matchers.matchesPattern(".*/api/v1/books/\\d+"));
    }

    @Test
    public void testReplaceBook() {
        String requestBody = """
                {
                    "title": "Updated book title",
                    "authorId": %d,
                    "genreId": %d,
                    "isbn": "1234567892",
                    "year": 2000
                }
                """.formatted(book.getAuthor().getId(), book.getGenre().getId());
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/api/v1/books/{id}", book.getId())
                .then()
                .log()
                .ifError()
                .statusCode(200)
                .body("id", Matchers.is(book.getId().intValue()))
                .body("title", Matchers.is("Updated book title"));
    }

    @Test
    public void testUpdateBook() {
        String requestBody = "{\"title\": \"Book 2 updated\"}";
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .patch("/api/v1/books/{id}", book.getId())
                .then()
                .log()
                .ifError()
                .statusCode(200)
                .body("id", Matchers.is(book.getId().intValue()))
                .body("title", Matchers.is("Book 2 updated"));

        bookRepository
                .findById(book.getId())
                .ifPresent(book1 -> Assertions.assertEquals("Book 2 updated", book1.getTitle()));
    }

    @Test
    public void testDeleteBook() {
        RestAssured.given()
                .when()
                .delete("/api/v1/books/{id}", book.getId())
                .then()
                .log()
                .ifError()
                .statusCode(204);
        Assertions.assertFalse(bookRepository.existsById(book.getId()));
    }
}
