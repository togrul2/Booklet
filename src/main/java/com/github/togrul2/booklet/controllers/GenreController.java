package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.genre.CreateGenreDto;
import com.github.togrul2.booklet.dtos.genre.GenreDto;
import com.github.togrul2.booklet.dtos.genre.UpdateGenreDto;
import com.github.togrul2.booklet.services.GenreService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@Tag(name = "Genres")
@RequestMapping("/api/v1/genres")
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    @Cacheable(cacheNames = "genres")
    public List<GenreDto> findAll() {
        return genreService.findAll();
    }

    @PostMapping
    @CacheEvict(cacheNames = "genres", allEntries = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Genre created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Taken attribute",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Void> create(@RequestBody @Valid CreateGenreDto createGenreDto) {
        GenreDto createdGenre = genreService.create(createGenreDto);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdGenre.id())
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    @Cacheable(cacheNames = "genre", key = "#id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Genre found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Genre not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public GenreDto findById(@PathVariable long id) {
        return genreService.findById(id);
    }

    @PutMapping("/{id}")
    @Caching(
            put = @CachePut(cacheNames = "genre", key = "#id"),
            evict = @CacheEvict(cacheNames = {"genres", "book", "books"}, allEntries = true)
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Genre created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Taken attribute",
                    content = @Content(mediaType = "application/json")
            )
    })
    public GenreDto replace(@PathVariable long id, @RequestBody @Valid CreateGenreDto createGenreDto) {
        return genreService.replace(id, createGenreDto);
    }

    @PatchMapping("/{id}")
    @Caching(
            put = @CachePut(cacheNames = "genre", key = "#id"),
            evict = @CacheEvict(cacheNames = {"genres", "book", "books"}, allEntries = true)
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Genre created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Taken attribute",
                    content = @Content(mediaType = "application/json")
            )
    })
    public GenreDto update(@PathVariable long id, @RequestBody @Valid UpdateGenreDto updateGenreDto) {
        return genreService.update(id, updateGenreDto);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "Genre deleted")
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "genre", key = "#id"),
                    @CacheEvict(cacheNames = {"genres", "book", "books"}, allEntries = true)
            }
    )
    public ResponseEntity<Void> delete(@PathVariable long id) {
        genreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
