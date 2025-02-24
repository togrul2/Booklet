package com.github.togrul2.booklet.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is used to handle exceptions that are thrown in the application.
 * It extends ResponseEntityExceptionHandler to provide more restful approach.
 *
 * @version 1.0
 * @see ResponseEntityExceptionHandler
 * @see AccessDeniedException
 * @see IllegalArgumentException
 * @see IllegalStateException
 * @see ExpiredJwtException
 * @see JwtException
 * @see EntityNotFoundException
 * @since 1.0
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        log.warn("Validation exception: {}", ex.getMessage());
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        Map<String, Set<String>> errorsMap = fieldErrors.stream().collect(
                Collectors.groupingBy(FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toSet())
                )
        );
        return ResponseEntity.badRequest().body(errorsMap);
    }

    /**
     * Handle AccessDeniedException. An exception is thrown when a user tries to access a resource that is not allowed.
     */
    @ExceptionHandler(AccessDeniedException.class)
    private void handleAccessDeniedException(RuntimeException e, HttpServletResponse response) throws IOException {
        log.warn("Access denied exception: {}", e.getMessage());
        response.sendError(HttpStatus.FORBIDDEN.value());
    }

    /**
     * Handle IllegalArgumentException and IllegalStateException.
     * An exception is thrown when a method receives an argument that is taken or creates a conflict in the database.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    private void handleConflictException(RuntimeException e, HttpServletResponse response) throws IOException {
        log.warn("Conflict exception: {}", e.getMessage());
        response.sendError(HttpStatus.CONFLICT.value());
    }

    /**
     * Handle ExpiredJwtException and JwtException.
     * An exception is thrown when a JWT token is expired or invalid.
     */
    @ExceptionHandler(JwtException.class)
    private void handleJwtException(RuntimeException e, HttpServletResponse response) throws IOException {
        log.warn("JWT exception: {}", e.getMessage());
        response.sendError(HttpStatus.UNAUTHORIZED.value());
    }
}
