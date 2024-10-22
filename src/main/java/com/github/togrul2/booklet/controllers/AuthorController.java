package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.author.AuthorDto;
import com.github.togrul2.booklet.dtos.author.CreateAuthorDto;
import com.github.togrul2.booklet.dtos.PaginationDto;
import com.github.togrul2.booklet.dtos.author.UpdateAuthorDto;
import com.github.togrul2.booklet.services.AuthorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/authors")
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping
    public Page<AuthorDto> getAuthors(@RequestParam @Valid PaginationDto paginationDto) {
        return authorService.findAll(paginationDto.getPageNumber(), paginationDto.getPageSize());
    }

    @GetMapping("/{id}")
    public AuthorDto getAuthor(@PathVariable long id) {
        return authorService.findOneById(id);
    }

    @PostMapping
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
    public AuthorDto replaceAuthor(@PathVariable long id, @RequestBody @Valid CreateAuthorDto createAuthorDto) {
        return authorService.replace(id, createAuthorDto);
    }

    @PatchMapping("/{id}")
    public AuthorDto updateAuthor(@PathVariable long id, @RequestBody @Valid UpdateAuthorDto updateAuthorDto) {
        return authorService.update(id, updateAuthorDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
