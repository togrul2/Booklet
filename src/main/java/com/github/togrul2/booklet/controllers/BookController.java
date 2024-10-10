package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.BookDto;
import com.github.togrul2.booklet.dtos.CreateBookDto;
import com.github.togrul2.booklet.dtos.UpdateBookDto;
import com.github.togrul2.booklet.services.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public Page<BookDto> getBooks(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") @Max(100) int pageSize
    ) {
        return bookService.findAll(pageNumber, pageSize);
    }

    @GetMapping("/{id}")
    public BookDto getBook(@PathVariable long id) {
        return bookService.findOneById(id);
    }

    @PostMapping
    public ResponseEntity<?> createBook(@RequestBody CreateBookDto createBookDto) {
        BookDto bookDto = bookService.create(createBookDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(bookDto.id())
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    public BookDto replaceBook(@PathVariable long id, @RequestBody @Valid CreateBookDto createBookDto) {
        return bookService.replace(id, createBookDto);
    }

    @PatchMapping("/{id}")
    public BookDto updateBook(@PathVariable long id, @RequestBody @Valid UpdateBookDto updateBookDto) {
        return bookService.update(id, updateBookDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
