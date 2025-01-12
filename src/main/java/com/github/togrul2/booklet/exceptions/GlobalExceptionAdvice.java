package com.github.togrul2.booklet.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;

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
@ControllerAdvice
public class GlobalExceptionAdvice extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

    /**
     * Handle AccessDeniedException. An exception is thrown when a user tries to access a resource that is not allowed.
     */
    @ExceptionHandler(AccessDeniedException.class)
    private void handleAccessDeniedException(RuntimeException e, HttpServletResponse response) throws IOException {
        logger.warn("Access denied exception: {}", e.getMessage());
        response.sendError(HttpStatus.FORBIDDEN.value());
    }

    /**
     * Handle IllegalArgumentException and IllegalStateException.
     * An exception is thrown when a method receives an argument that is taken or creates a conflict in the database.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    private void handleConflictException(RuntimeException e, HttpServletResponse response) throws IOException {
        logger.warn("Conflict exception: {}", e.getMessage());
        response.sendError(HttpStatus.CONFLICT.value());
    }

    /**
     * Handle ExpiredJwtException and JwtException.
     * An exception is thrown when a JWT token is expired or invalid.
     */
    @ExceptionHandler(JwtException.class)
    private void handleJwtException(RuntimeException e, HttpServletResponse response) throws IOException {
        logger.warn("JWT exception: {}", e.getMessage());
        response.sendError(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * Handle EntityNotFoundException
     * An exception is thrown when an entity is not found in the database.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    private void handleEntityNotFoundException(RuntimeException e, HttpServletResponse response) throws IOException {
        logger.warn("Entity not found exception: {}", e.getMessage());
        response.sendError(HttpStatus.NOT_FOUND.value());
    }
}
