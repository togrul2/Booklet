package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.CreateGenreDto;
import com.github.togrul2.booklet.dtos.GenreDto;
import com.github.togrul2.booklet.dtos.UpdateGenreDto;
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
                .when(genreRepository.findById(genre.getId()))
                .thenReturn(Optional.of(genre));
        GenreDto resultGenre = genreService.findOneById(genre.getId());
        Assertions.assertEquals(genre.getId(), resultGenre.id());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(genre.getId());
    }

    @Test
    public void testGetGenreByIdNotFound() {
        Mockito
                .when(genreRepository.findById(genre.getId()))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(GenreNotFound.class, () -> genreService.findOneById(genre.getId()));
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(genre.getId());
    }

    @Test
    public void testCreateGenre() {
        Mockito
                .when(genreRepository.save(Mockito.any(Genre.class)))
                .thenReturn(genre);
        Mockito
                .when(genreRepository.existsByName(genre.getName()))
                .thenReturn(false);
        Mockito
                .when(genreRepository.existsBySlug(genre.getSlug()))
                .thenReturn(false);

        GenreDto resultGenre = genreService.create(new CreateGenreDto("Test genre", "test-genre"));

        Assertions.assertEquals(genre.getId(), resultGenre.id());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .save(Mockito.any(Genre.class));
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsByName(genre.getName());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsBySlug(genre.getSlug());
    }

    @Test
    public void testReplaceGenre() {
        CreateGenreDto createGenreDto = new CreateGenreDto("Test genre", "test-genre");
        Genre createGenre = GenreMapper.INSTANCE.toGenre(createGenreDto);
        createGenre.setId(genre.getId());

        Mockito
                .when(genreRepository.findById(genre.getId()))
                .thenReturn(Optional.of(genre));
        Mockito
                .when(genreRepository.existsById(genre.getId()))
                .thenReturn(true);
        Mockito
                .when(genreRepository.save(Mockito.any(Genre.class)))
                .thenReturn(genre);
        Mockito
                .when(genreRepository.existsByNameAndIdNot(genre.getName(), genre.getId()))
                .thenReturn(false);
        Mockito
                .when(genreRepository.existsBySlugAndIdNot(genre.getSlug(), genre.getId()))
                .thenReturn(false);

        GenreDto resultGenre = genreService.replace(genre.getId(), createGenreDto);

        Assertions.assertEquals(genre.getId(), resultGenre.id());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsById(genre.getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsByNameAndIdNot(genre.getName(), genre.getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .existsBySlugAndIdNot(genre.getSlug(), genre.getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .save(Mockito.any(Genre.class));
    }

    @Test
    public void testUpdateGenre() {
        Mockito
                .when(genreRepository.findById(genre.getId()))
                .thenReturn(Optional.of(genre));
        Mockito
                .when(genreRepository.existsByNameAndIdNot(genre.getName(), genre.getId()))
                .thenReturn(false);
        Mockito
                .when(genreRepository.save(Mockito.any(Genre.class)))
                .thenReturn(genre);

        GenreDto resultGenre = genreService.update(
                genre.getId(), UpdateGenreDto.builder().name("Test genre").build()
        );

        Assertions.assertEquals(genre.getId(), resultGenre.id());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .findById(genre.getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .save(Mockito.any(Genre.class));
    }

    @Test
    public void testDeleteGenre() {
        Mockito
                .doNothing()
                .when(genreRepository)
                .deleteById(genre.getId());
        genreService.delete(genre.getId());
        Mockito
                .verify(genreRepository, Mockito.times(1))
                .deleteById(genre.getId());
    }
}
