package com.github.togrul2.booklet.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A DTO class to represent the error response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class ErrorDto {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
