package com.github.togrul2.booklet.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AuthorNotFound extends RuntimeException {
    public AuthorNotFound() {
        super("Author not found.");
    }
}
