package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.CreateGenreDto;
import com.github.togrul2.booklet.dtos.GenreDto;
import com.github.togrul2.booklet.dtos.UpdateGenreDto;
import com.github.togrul2.booklet.services.GenreService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/genres")
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public List<GenreDto> getGenres() {
        return genreService.findAll();
    }

    @GetMapping("/{id}")
    public GenreDto getGenreById(@PathVariable long id) {
        return genreService.findOneById(id);
    }

    @PostMapping
    public ResponseEntity<?> createGenre(@RequestBody @Valid CreateGenreDto createGenreDto) {
        GenreDto createdGenre = genreService.create(createGenreDto);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdGenre.id())
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    public GenreDto replaceGenre(@PathVariable long id, @RequestBody @Valid CreateGenreDto createGenreDto) {
        return genreService.replace(id, createGenreDto);
    }

    @PatchMapping("/{id}")
    public GenreDto updateGenre(@PathVariable long id, @RequestBody @Valid UpdateGenreDto updateGenreDto) {
        return genreService.update(id, updateGenreDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGenre(@PathVariable long id) {
        genreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
