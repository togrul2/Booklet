package com.github.togrul2.booklet.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
 * @since 1.0
 */
@ControllerAdvice
public class GlobalExceptionAdvice extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(RuntimeException e, HttpServletResponse response) throws IOException {
        logger.warn("Access denied exception: {}", e.getMessage());
        response.sendError(HttpStatus.FORBIDDEN.value());
    }

    /**
     * Handle IllegalArgumentException and IllegalStateException.
     * An exception is thrown when a method receives an argument that is taken or creates a conflict in the database.
     *
     * @param e        The exception that was thrown.
     * @param response The response that will be sent.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    protected void handleConflictException(RuntimeException e, HttpServletResponse response) throws IOException {
        logger.warn("Conflict exception: {}", e.getMessage());
        response.sendError(HttpStatus.CONFLICT.value());
    }

    /**
     * Handle ExpiredJwtException and JwtException.
     * An exception is thrown when a JWT token is expired or invalid.
     *
     * @param e        The exception that was thrown.
     * @param response The response that will be sent.
     */
    @ExceptionHandler(JwtException.class)
    public void handleJwtException(RuntimeException e, HttpServletResponse response) throws IOException {
        logger.warn("JWT exception: {}", e.getMessage());
        response.sendError(HttpStatus.UNAUTHORIZED.value());
    }
}
