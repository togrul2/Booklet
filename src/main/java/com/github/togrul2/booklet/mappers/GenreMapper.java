package com.github.togrul2.booklet.mappers;

import com.github.togrul2.booklet.dtos.genre.CreateGenreDto;
import com.github.togrul2.booklet.dtos.genre.GenreDto;
import com.github.togrul2.booklet.entities.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenreMapper {
    GenreMapper INSTANCE = Mappers.getMapper(GenreMapper.class);

    GenreDto toGenreDto(Genre genre);

    @Mapping(target = "id", ignore = true)
    Genre toGenre(CreateGenreDto createGenreDto);
}
