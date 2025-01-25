package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.annotations.ApiErrorResponses;
import com.github.togrul2.booklet.dtos.book.BookDto;
import com.github.togrul2.booklet.dtos.book.BookFilterDto;
import com.github.togrul2.booklet.dtos.book.CreateBookDto;
import com.github.togrul2.booklet.dtos.book.UpdateBookDto;
import com.github.togrul2.booklet.services.BookService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@ApiErrorResponses
@AllArgsConstructor
@Tag(name = "Books")
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    @Cacheable(cacheNames = "books", key = "#pageable + ';' + #filterDto")
    @ApiResponse(responseCode = "200", description = "Ok")
    public Page<BookDto> getBooks(@ParameterObject Pageable pageable, @ParameterObject @Valid BookFilterDto filterDto) {
        return bookService.findAll(pageable, filterDto);
    }

    @GetMapping("/{id}")
    @Cacheable(cacheNames = "book", key = "#id")
    @ApiResponse(responseCode = "200", description = "Ok")
    public BookDto getBook(@PathVariable long id) {
        return bookService.findById(id);
    }

    @PostMapping
    @CacheEvict(cacheNames = "books", allEntries = true)
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json"))
    public ResponseEntity<Void> createBook(@RequestBody CreateBookDto createBookDto) {
        BookDto bookDto = bookService.create(createBookDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(bookDto.id())
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    @Caching(
            put = @CachePut(cacheNames = "book", key = "#id"),
            evict = @CacheEvict(cacheNames = "books", allEntries = true)
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))
    )
    public BookDto replaceBook(@PathVariable long id, @RequestBody @Valid CreateBookDto createBookDto) {
        return bookService.replace(id, createBookDto);
    }

    @PatchMapping("/{id}")
    @Caching(
            put = @CachePut(cacheNames = "book", key = "#id"),
            evict = @CacheEvict(cacheNames = "books", allEntries = true)
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))
    )
    public BookDto updateBook(@PathVariable long id, @RequestBody @Valid UpdateBookDto updateBookDto) {
        return bookService.update(id, updateBookDto);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "No content")
    @Caching(evict = {
            @CacheEvict(cacheNames = "book", key = "#id"),
            @CacheEvict(cacheNames = "books", allEntries = true)
    })
    public ResponseEntity<Void> deleteBook(@PathVariable long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
