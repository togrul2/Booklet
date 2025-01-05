package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.author.AuthorDto;
import com.github.togrul2.booklet.dtos.author.CreateAuthorDto;
import com.github.togrul2.booklet.dtos.author.UpdateAuthorDto;
import com.github.togrul2.booklet.services.AuthorService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@AllArgsConstructor
@Tag(name = "Authors")
@RequestMapping("/api/v1/authors")
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping
    @Cacheable(cacheNames = "authors", key = "#pageable")
    public Page<AuthorDto> getAuthors(@ParameterObject Pageable pageable) {
        return authorService.findAll(pageable);
    }

    @Cacheable(cacheNames = "author", key = "#id")
    @GetMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public AuthorDto getAuthor(@PathVariable long id) {
        return authorService.findOneById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Author created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Author already exists",
                    content = @Content(mediaType = "application/json")
            )
    })
    @CacheEvict(cacheNames = "authors", allEntries = true)
    public ResponseEntity<Void> createAuthor(@RequestBody @Valid CreateAuthorDto createAuthorDto) {
        AuthorDto authorDto = authorService.create(createAuthorDto);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(authorDto.id())
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Caching(
            put = @CachePut(cacheNames = "author", key = "#id"),
            evict = @CacheEvict(cacheNames = "authors", allEntries = true)
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Author created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Author already exists",
                    content = @Content(mediaType = "application/json")
            )
    })
    public AuthorDto replaceAuthor(@PathVariable long id, @RequestBody @Valid CreateAuthorDto createAuthorDto) {
        return authorService.replace(id, createAuthorDto);
    }

    @Caching(
            put = @CachePut(cacheNames = "author", key = "#id"),
            evict = @CacheEvict(cacheNames = "authors", allEntries = true)
    )
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Author created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Author already exists",
                    content = @Content(mediaType = "application/json")
            )
    })
    public AuthorDto updateAuthor(@PathVariable long id, @RequestBody @Valid UpdateAuthorDto updateAuthorDto) {
        return authorService.update(id, updateAuthorDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "204", description = "Author deleted")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "author", key = "#id"),
                    @CacheEvict(cacheNames = "authors", allEntries = true)
            }
    )
    public ResponseEntity<Void> deleteAuthor(@PathVariable long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
