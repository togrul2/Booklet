package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.AuthorDto;
import com.github.togrul2.booklet.dtos.CreateAuthorDto;
import com.github.togrul2.booklet.dtos.UpdateAuthorDto;
import com.github.togrul2.booklet.entities.Author;
import com.github.togrul2.booklet.exceptions.AuthorNotFound;
import com.github.togrul2.booklet.mappers.AuthorMapper;
import com.github.togrul2.booklet.repositories.AuthorRepository;
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

@ExtendWith(SpringExtension.class)
public class AuthorServiceTests {
    @InjectMocks
    private AuthorService authorService;
    @Mock
    private AuthorRepository authorRepository;

    private List<Author> authors;
    private Author author;

    @BeforeEach
    public void setUp() {
        this.author = Author
                .builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .build();
        this.authors = List.of(author);
    }

    @Test
    public void testGetAllAuthors() {
        Mockito
                .when(authorRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(authors));
        Page<AuthorDto> authors = authorService.findAll(1, 10);
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .findAll(PageRequest.of(0, 10));
        Assertions.assertEquals(1, authors.getTotalElements());
    }

    @Test
    public void testGetAuthorById() {
        Mockito
                .when(authorRepository.findById(author.getId()))
                .thenReturn(java.util.Optional.of(author));
        AuthorDto resultAuthor = authorService.findOneById(author.getId());
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .findById(author.getId());
        Assertions.assertEquals(author.getId(), resultAuthor.id());
    }

    @Test
    public void testGetAuthorByIdNotFound() {
        Mockito
                .when(authorRepository.findById(author.getId()))
                .thenReturn(java.util.Optional.empty());
        Assertions.assertThrows(AuthorNotFound.class, () -> authorService.findOneById(author.getId()));
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .findById(author.getId());
    }

    @Test
    public void testCreateAuthor() {
        CreateAuthorDto createAuthorDto = CreateAuthorDto
                .builder()
                .name("John")
                .surname("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .biography("Test biography")
                .build();
        Mockito
                .when(authorRepository.save(AuthorMapper.INSTANCE.toAuthor(createAuthorDto)))
                .thenReturn(author);
        AuthorDto createResult = authorService.create(createAuthorDto);
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .save(Mockito.any(Author.class));
        Assertions.assertEquals(author.getId(), createResult.id());
    }

    @Test
    public void testReplaceAuthor() {
        CreateAuthorDto createAuthorDto = CreateAuthorDto
                .builder()
                .name("John")
                .surname("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .biography("Updated test biography")
                .build();

        Author resultAuthor = AuthorMapper.INSTANCE.toAuthor(createAuthorDto);
        resultAuthor.setId(author.getId());

        Mockito
                .when(authorRepository.existsById(author.getId()))
                .thenReturn(true);
        Mockito
                .when(authorRepository.save(Mockito.any(Author.class)))
                .thenReturn(resultAuthor);

        AuthorDto replaceResult = authorService.replace(author.getId(), createAuthorDto);

        Mockito
                .verify(authorRepository, Mockito.times(1))
                .save(Mockito.any(Author.class));
        Assertions.assertEquals(author.getId(), replaceResult.id());
        Assertions.assertEquals("Updated test biography", replaceResult.biography());
    }

    @Test
    public void testUpdateAuthor() {
        Mockito
                .when(authorRepository.findById(author.getId()))
                .thenReturn(java.util.Optional.of(author));
        Mockito
                .when(authorRepository.save(author))
                .thenReturn(author);
        AuthorDto updateResult = authorService.update(
                author.getId(),
                UpdateAuthorDto
                        .builder()
                        .name("John")
                        .surname("Doe")
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .biography("Updated test biography")
                        .build()
        );
        Assertions.assertEquals(author.getId(), updateResult.id());
        Assertions.assertEquals("Updated test biography", updateResult.biography());
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .findById(author.getId());
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .save(author);
    }

    @Test
    public void testDeleteAuthor() {
        Mockito
                .doNothing()
                .when(authorRepository)
                .deleteById(author.getId());
        Assertions.assertDoesNotThrow(() -> authorService.delete(author.getId()));
        Mockito
                .verify(authorRepository, Mockito.times(1))
                .deleteById(author.getId());
    }
}
