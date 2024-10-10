package com.github.togrul2.booklet.mappers;

import com.github.togrul2.booklet.dtos.BookDto;
import com.github.togrul2.booklet.dtos.CreateBookDto;
import com.github.togrul2.booklet.entities.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BookMapper {
    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    BookDto toBookDto(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "genre", ignore = true)
    Book toBook(CreateBookDto createBookDto);
}
