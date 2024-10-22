package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.book.BookDto;
import com.github.togrul2.booklet.dtos.book.CreateBookDto;
import com.github.togrul2.booklet.dtos.PaginationDto;
import com.github.togrul2.booklet.dtos.book.UpdateBookDto;
import com.github.togrul2.booklet.services.BookService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public Page<BookDto> getBooks(@RequestParam @Valid PaginationDto paginationDto) {
        return bookService.findAll(paginationDto.getPageNumber(), paginationDto.getPageSize());
    }

    @GetMapping("/{id}")
    public BookDto getBook(@PathVariable long id) {
        return bookService.findOneById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:write')")
    public ResponseEntity<Void> createBook(@RequestBody CreateBookDto createBookDto) {
        BookDto bookDto = bookService.create(createBookDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(bookDto.id())
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:write')")
    public BookDto replaceBook(@PathVariable long id, @RequestBody @Valid CreateBookDto createBookDto) {
        return bookService.replace(id, createBookDto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:write')")
    public BookDto updateBook(@PathVariable long id, @RequestBody @Valid UpdateBookDto updateBookDto) {
        return bookService.update(id, updateBookDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:write')")
    public ResponseEntity<Void> deleteBook(@PathVariable long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
