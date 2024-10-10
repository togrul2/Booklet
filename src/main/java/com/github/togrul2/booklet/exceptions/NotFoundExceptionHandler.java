package com.github.togrul2.booklet.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class NotFoundExceptionHandler {
    @ResponseBody
    @ExceptionHandler({BookNotFound.class, GenreNotFound.class, AuthorNotFound.class})
    public ResponseEntity<?> handleNotFound() {
        return ResponseEntity.notFound().build();
    }
}
