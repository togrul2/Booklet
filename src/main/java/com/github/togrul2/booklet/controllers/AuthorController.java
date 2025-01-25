package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.annotations.ApiErrorResponses;
import com.github.togrul2.booklet.dtos.author.AuthorDto;
import com.github.togrul2.booklet.dtos.author.AuthorFilterDto;
import com.github.togrul2.booklet.dtos.author.CreateAuthorDto;
import com.github.togrul2.booklet.dtos.author.UpdateAuthorDto;
import com.github.togrul2.booklet.services.AuthorService;
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
@Tag(name = "Authors")
@RequestMapping("/api/v1/authors")
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Ok")
    @Cacheable(cacheNames = "authors", key = "#pageable + ';' + #filterDto")
    public Page<AuthorDto> findAll(@ParameterObject Pageable pageable, @ParameterObject AuthorFilterDto filterDto) {
        return authorService.findAll(pageable, filterDto);
    }

    @GetMapping("/{id}")
    @Cacheable(cacheNames = "author", key = "#id")
    @ApiResponse(responseCode = "200", description = "Ok")
    public AuthorDto findById(@PathVariable long id) {
        return authorService.findById(id);
    }

    @PostMapping
    @CacheEvict(cacheNames = "authors", allEntries = true)
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json"))
    public ResponseEntity<Void> create(@RequestBody @Valid CreateAuthorDto createAuthorDto) {
        AuthorDto authorDto = authorService.create(createAuthorDto);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(authorDto.id())
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorDto.class))
    )
    @Caching(
            put = @CachePut(cacheNames = "author", key = "#id"),
            evict = @CacheEvict(cacheNames = {"authors", "books", "book"}, allEntries = true)
    )
    public AuthorDto replace(@PathVariable long id, @RequestBody @Valid CreateAuthorDto createAuthorDto) {
        return authorService.replace(id, createAuthorDto);
    }

    @PatchMapping("/{id}")
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorDto.class))
    )
    @Caching(
            put = @CachePut(cacheNames = "author", key = "#id"),
            evict = @CacheEvict(cacheNames = {"authors", "books", "book"}, allEntries = true)
    )
    public AuthorDto update(@PathVariable long id, @RequestBody @Valid UpdateAuthorDto updateAuthorDto) {
        return authorService.update(id, updateAuthorDto);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "No content")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "author", key = "#id"),
                    @CacheEvict(cacheNames = {"authors", "books", "book"}, allEntries = true)
            }
    )
    public ResponseEntity<Void> delete(@PathVariable long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
