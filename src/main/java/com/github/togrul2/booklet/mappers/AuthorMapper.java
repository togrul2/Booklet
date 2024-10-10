package com.github.togrul2.booklet.mappers;

import com.github.togrul2.booklet.dtos.AuthorDto;
import com.github.togrul2.booklet.dtos.CreateAuthorDto;
import com.github.togrul2.booklet.entities.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthorMapper {
    AuthorMapper INSTANCE = Mappers.getMapper(AuthorMapper.class);

    AuthorDto toAuthorDto(Author author);

    @Mapping(target = "id", ignore = true)
    Author toAuthor(CreateAuthorDto authorDto);
}
