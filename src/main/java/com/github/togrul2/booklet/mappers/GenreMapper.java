package com.github.togrul2.booklet.mappers;

import com.github.togrul2.booklet.dtos.CreateGenreDto;
import com.github.togrul2.booklet.dtos.GenreDto;
import com.github.togrul2.booklet.entities.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface GenreMapper {
    GenreMapper INSTANCE = Mappers.getMapper(GenreMapper.class);

    GenreDto toGenreDto(Genre genre);

    List<GenreDto> toGenreDtoList(List<Genre> genres);

    @Mapping(target = "id", ignore = true)
    Genre toGenre(CreateGenreDto createGenreDto);
}
