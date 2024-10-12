package com.github.togrul2.booklet.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TakenAttributeException extends RuntimeException {
    public TakenAttributeException(String message) {
        super(message);
    }
}
