package com.github.togrul2.booklet.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginDto(
        @Email
        String email,
        @NotNull
        @NotBlank
        String password
) {
}
