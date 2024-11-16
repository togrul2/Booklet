package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.genre.CreateGenreDto;
import com.github.togrul2.booklet.dtos.genre.GenreDto;
import com.github.togrul2.booklet.dtos.genre.UpdateGenreDto;
import com.github.togrul2.booklet.entities.Genre;
import com.github.togrul2.booklet.exceptions.GenreNotFound;
import com.github.togrul2.booklet.mappers.GenreMapper;
import com.github.togrul2.booklet.repositories.GenreRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class GenreServiceTests {
    @Mock
    private GenreRepository genreRepository;
    @InjectMocks
    private GenreService genreService;

    private List<Genre> genres;
    private Genre genre;

    @BeforeEach
    public void setUp() {
        this.genre = new Genre(1L, "Test genre", "test-genre");
        this.genres = List.of(genre);
    }

    @Test
    public void testGetAllGenres() {
        Mockito
                .when(genreRepository.findAll())
                .thenReturn(genres);
        List<GenreDto> resultGenres = genreService.findAll();
        Assertions.assertEquals(1, resultGenres.size());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findAll();
    }

    @Test
    public void testGetGenreById() {
        Mockito
                .when(genreRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(genre));
        GenreDto resultGenre = genreService.findOneById(genre.getId());
        Assertions.assertEquals(genre.getId(), resultGenre.id());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
    }

    @Test
    public void testGetGenreByIdNotFound() {
        Mockito
                .when(genreRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(GenreNotFound.class, () -> genreService.findOneById(genre.getId()));
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
    }

    @Test
    public void testCreateGenre() {
        Mockito
                .when(genreRepository.save(Mockito.any(Genre.class)))
                .thenReturn(genre);
        Mockito
                .when(genreRepository.existsByName(Mockito.anyString()))
                .thenReturn(false);
        Mockito
                .when(genreRepository.existsBySlug(Mockito.anyString()))
                .thenReturn(false);

        GenreDto resultGenre = genreService.create(new CreateGenreDto("Test genre", "test-genre"));

        Assertions.assertEquals(genre.getId(), resultGenre.id());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .save(Mockito.any(Genre.class));
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsByName(Mockito.anyString());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsBySlug(Mockito.anyString());
    }

    @Test
    public void testCreateGenreWithTakenName() {
        Mockito
                .when(genreRepository.existsByName(Mockito.anyString()))
                .thenReturn(true);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> genreService.create(new CreateGenreDto("Test genre", "test-genre"))
        );
        Mockito
                .verify(genreRepository, Mockito.times(0))
                .save(Mockito.any(Genre.class));
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsByName(Mockito.anyString());
        Mockito
                .verify(genreRepository, Mockito.times(0))
                .existsBySlug(Mockito.anyString());
    }

    @Test
    public void testCreateGenreWithTakenSlug() {
        Mockito
                .when(genreRepository.existsByName(Mockito.anyString()))
                .thenReturn(false);
        Mockito
                .when(genreRepository.existsBySlug(Mockito.anyString()))
                .thenReturn(true);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> genreService.create(new CreateGenreDto("Test genre", "test-genre"))
        );
        Mockito
                .verify(genreRepository, Mockito.times(0))
                .save(Mockito.any(Genre.class));
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsByName(Mockito.anyString());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsBySlug(Mockito.anyString());
    }

    @Test
    public void testReplaceGenre() {
        CreateGenreDto createGenreDto = new CreateGenreDto("Test genre", "test-genre");
        Genre createGenre = GenreMapper.INSTANCE.toGenre(createGenreDto);
        createGenre.setId(genre.getId());

        Mockito
                .when(genreRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(genre));
        Mockito
                .when(genreRepository.existsById(Mockito.anyLong()))
                .thenReturn(true);
        Mockito
                .when(genreRepository.save(Mockito.any(Genre.class)))
                .thenReturn(genre);
        Mockito
                .when(genreRepository.existsByNameAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(false);
        Mockito
                .when(genreRepository.existsBySlugAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(false);

        GenreDto resultGenre = genreService.replace(genre.getId(), createGenreDto);

        Assertions.assertEquals(genre.getId(), resultGenre.id());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsByNameAndIdNot(Mockito.anyString(), Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsBySlugAndIdNot(Mockito.anyString(), Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .save(Mockito.any(Genre.class));
    }

    @Test
    public void testReplaceGenreNotFound() {
        CreateGenreDto createGenreDto = new CreateGenreDto("Test genre", "test-genre");

        Mockito
                .when(genreRepository.existsById(Mockito.anyLong()))
                .thenReturn(false);

        Assertions.assertThrows(
                GenreNotFound.class,
                () -> genreService.replace(genre.getId(), createGenreDto)
        );
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(0))
                .save(Mockito.any(Genre.class));
    }

    @Test
    public void testReplaceGenreWithTakenName() {
        CreateGenreDto createGenreDto = new CreateGenreDto("Test genre", "test-genre");

        Mockito
                .when(genreRepository.existsById(Mockito.anyLong()))
                .thenReturn(true);
        Mockito
                .when(genreRepository.existsByNameAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(true);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> genreService.replace(genre.getId(), createGenreDto)
        );
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsByNameAndIdNot(Mockito.anyString(), Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(0))
                .save(Mockito.any(Genre.class));
    }

    @Test
    public void testReplaceGenreWithTakenSlug() {
        CreateGenreDto createGenreDto = new CreateGenreDto("Test genre", "test-genre");

        Mockito
                .when(genreRepository.existsById(Mockito.anyLong()))
                .thenReturn(true);
        Mockito
                .when(genreRepository.existsByNameAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(false);
        Mockito
                .when(genreRepository.existsBySlugAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(true);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> genreService.replace(genre.getId(), createGenreDto)
        );
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsById(genre.getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsByNameAndIdNot(Mockito.anyString(), Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsBySlugAndIdNot(Mockito.anyString(), Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(0))
                .save(Mockito.any(Genre.class));
    }

    @Test
    public void testUpdateGenre() {
        Mockito
                .when(genreRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(genre));
        Mockito
                .when(genreRepository.existsByNameAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(false);
        Mockito
                .when(genreRepository.save(Mockito.any(Genre.class)))
                .thenReturn(genre);

        GenreDto resultGenre = genreService.update(
                genre.getId(),
                new UpdateGenreDto("Test genre", "test-genre")
        );

        Assertions.assertEquals(genre.getId(), resultGenre.id());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .save(Mockito.any(Genre.class));
    }

    @Test
    public void testUpdateGenreWithTakenName() {
        Mockito
                .when(genreRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(genre));
        Mockito
                .when(genreRepository.existsByNameAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(true);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> genreService.update(genre.getId(), new UpdateGenreDto("Test name", "test-name"))
        );

        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsByNameAndIdNot(Mockito.anyString(), Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(0))
                .save(Mockito.any(Genre.class));
    }

    @Test
    public void testUpdateGenreWithTakenSlug() {
        Mockito
                .when(genreRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(genre));
        Mockito
                .when(genreRepository.existsByNameAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(false);
        Mockito
                .when(genreRepository.existsBySlugAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(true);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> genreService.update(genre.getId(), new UpdateGenreDto("Test genre", "test-genre"))
        );

        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsByNameAndIdNot(Mockito.anyString(), Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsBySlugAndIdNot(Mockito.anyString(), Mockito.anyLong());
        Mockito
                .verify(genreRepository, Mockito.times(0))
                .save(Mockito.any(Genre.class));
    }


    @Test
    public void testDeleteGenre() {
        Mockito
                .doNothing()
                .when(genreRepository)
                .deleteById(Mockito.anyLong());
        genreService.delete(genre.getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .deleteById(Mockito.anyLong());
    }
}
