package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.book.BookDto;
import com.github.togrul2.booklet.dtos.book.CreateBookDto;
import com.github.togrul2.booklet.dtos.book.UpdateBookDto;
import com.github.togrul2.booklet.services.BookService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@AllArgsConstructor
@Tag(name = "Books")
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    @Cacheable(cacheNames = "books", key = "#pageable")
    public Page<BookDto> getBooks(@ParameterObject Pageable pageable) {
        return bookService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Cacheable(cacheNames = "book", key = "#id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public BookDto getBook(@PathVariable long id) {
        return bookService.findOneById(id);
    }

    @PostMapping
    @CacheEvict(cacheNames = "books", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Taken attribute",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Void> createBook(@RequestBody CreateBookDto createBookDto) {
        BookDto bookDto = bookService.create(createBookDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(bookDto.id())
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Caching(
            put = @CachePut(cacheNames = "book", key = "#id"),
            evict = @CacheEvict(cacheNames = "books", allEntries = true)
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Taken attribute",
                    content = @Content(mediaType = "application/json")
            )
    })
    public BookDto replaceBook(@PathVariable long id, @RequestBody @Valid CreateBookDto createBookDto) {
        return bookService.replace(id, createBookDto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Caching(
            put = @CachePut(cacheNames = "book", key = "#id"),
            evict = @CacheEvict(cacheNames = "books", allEntries = true)
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Taken attribute",
                    content = @Content(mediaType = "application/json")
            )
    })
    public BookDto updateBook(@PathVariable long id, @RequestBody @Valid UpdateBookDto updateBookDto) {
        return bookService.update(id, updateBookDto);
    }

    @DeleteMapping("/{id}")
    @Caching(evict = {
            @CacheEvict(cacheNames = "book", key = "#id"),
            @CacheEvict(cacheNames = "books", allEntries = true)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "204", description = "Book deleted")
    public ResponseEntity<Void> deleteBook(@PathVariable long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
