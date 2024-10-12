package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.CreateGenreDto;
import com.github.togrul2.booklet.dtos.GenreDto;
import com.github.togrul2.booklet.dtos.UpdateGenreDto;
import com.github.togrul2.booklet.entities.Genre;
import com.github.togrul2.booklet.exceptions.TakenAttributeException;
import com.github.togrul2.booklet.exceptions.GenreNotFound;
import com.github.togrul2.booklet.mappers.GenreMapper;
import com.github.togrul2.booklet.repositories.GenreRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public List<GenreDto> findAll() {
        return GenreMapper.INSTANCE.toGenreDtoList(genreRepository.findAll());
    }

    public GenreDto findOneById(long id) {
        return GenreMapper.INSTANCE.toGenreDto(genreRepository.findById(id).orElseThrow(GenreNotFound::new));
    }

    private void validateGenreData(CreateGenreDto createGenreDto) {
        if (genreRepository.existsByName(createGenreDto.name()))
            throw new TakenAttributeException("Genre with this name already exists");
        if (genreRepository.existsBySlug(createGenreDto.slug()))
            throw new TakenAttributeException("Genre with this slug already exists");
    }

    private void validateGenreData(CreateGenreDto createGenreDto, long id) {
        if (genreRepository.existsByNameAndIdNot(createGenreDto.name(), id))
            throw new TakenAttributeException("Genre with this name already exists");
        if (genreRepository.existsBySlugAndIdNot(createGenreDto.slug(), id))
            throw new TakenAttributeException("Genre with this slug already exists");
    }

    private void validateGenreData(UpdateGenreDto updateGenreDto, long id) {
        if ((updateGenreDto.name() != null) && genreRepository.existsByNameAndIdNot(updateGenreDto.name(), id))
            throw new TakenAttributeException("Genre with this name already exists");
        if ((updateGenreDto.slug() != null) && genreRepository.existsBySlugAndIdNot(updateGenreDto.slug(), id))
            throw new TakenAttributeException("Genre with this slug already exists");
    }

    public GenreDto create(CreateGenreDto createGenreDto) {
        validateGenreData(createGenreDto);
        return GenreMapper.INSTANCE.toGenreDto(
                genreRepository.save(GenreMapper.INSTANCE.toGenre(createGenreDto))
        );
    }

    public GenreDto replace(long id, CreateGenreDto createGenreDto) {
        if (!genreRepository.existsById(id))
            throw new GenreNotFound();

        validateGenreData(createGenreDto, id);
        Genre genre = GenreMapper.INSTANCE.toGenre(createGenreDto);
        genre.setId(id);
        return GenreMapper.INSTANCE.toGenreDto(genreRepository.save(genre));
    }

    public GenreDto update(long id, UpdateGenreDto updateGenreDto) {
        Genre genre = genreRepository.findById(id).orElseThrow(GenreNotFound::new);
        validateGenreData(updateGenreDto, id);

        if (updateGenreDto.name() != null)
            genre.setName(updateGenreDto.name());
        if (updateGenreDto.slug() != null)
            genre.setSlug(updateGenreDto.slug());

        return GenreMapper.INSTANCE.toGenreDto(genreRepository.save(genre));
    }

    public void delete(long id) {
        genreRepository.deleteById(id);
    }
}
