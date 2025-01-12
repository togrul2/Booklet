package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.genre.CreateGenreDto;
import com.github.togrul2.booklet.dtos.genre.GenreDto;
import com.github.togrul2.booklet.dtos.genre.UpdateGenreDto;
import com.github.togrul2.booklet.entities.Genre;
import com.github.togrul2.booklet.exceptions.GenreNotFound;
import com.github.togrul2.booklet.mappers.GenreMapper;
import com.github.togrul2.booklet.repositories.GenreRepository;
import com.github.togrul2.booklet.security.annotations.IsAdmin;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public List<GenreDto> findAll() {
        return genreRepository
                .findAll()
                .stream()
                .map(GenreMapper.INSTANCE::toGenreDto)
                .collect(Collectors.toList());
    }

    public GenreDto findById(long id) {
        return genreRepository
                .findById(id)
                .map(GenreMapper.INSTANCE::toGenreDto)
                .orElseThrow(GenreNotFound::new);
    }

    private void validateGenre(Genre genre) {
        genreRepository.findByName(genre.getName()).ifPresent(g -> {
            if (g.getId() == null || !Objects.equals(g.getId(), genre.getId())) {
                throw new IllegalArgumentException("Genre with this name already exists.");
            }
        });

        genreRepository.findBySlug(genre.getSlug()).ifPresent(g -> {
            if (g.getId() == null || !Objects.equals(g.getId(), genre.getId())) {
                throw new IllegalArgumentException("Genre with this slug already exists.");
            }
        });
    }

    @IsAdmin
    public GenreDto create(CreateGenreDto createGenreDto) {
        Genre genre = GenreMapper.INSTANCE.toGenre(createGenreDto);
        validateGenre(genre);
        return GenreMapper.INSTANCE.toGenreDto(genreRepository.save(genre));
    }

    @IsAdmin
    public GenreDto replace(long id, CreateGenreDto createGenreDto) {
        // Validate if given genre exists.
        if (!genreRepository.existsById(id)) {
            throw new GenreNotFound();
        }

        // Create genre.
        Genre genre = GenreMapper.INSTANCE.toGenre(createGenreDto);
        genre.setId(id);

        validateGenre(genre);
        return GenreMapper.INSTANCE.toGenreDto(genreRepository.save(genre));
    }

    @IsAdmin
    public GenreDto update(long id, UpdateGenreDto updateGenreDto) {
        Genre genre = genreRepository.findById(id).orElseThrow(GenreNotFound::new);

        if (updateGenreDto.name() != null) {
            genre.setName(updateGenreDto.name());
        }

        if (updateGenreDto.slug() != null) {
            genre.setSlug(updateGenreDto.slug());
        }

        validateGenre(genre);
        return GenreMapper.INSTANCE.toGenreDto(genreRepository.save(genre));
    }

    @IsAdmin
    public void delete(long id) {
        genreRepository.deleteById(id);
    }
}
