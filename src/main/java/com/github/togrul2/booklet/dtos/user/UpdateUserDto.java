package com.github.togrul2.booklet.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserDto(
        @Email
        String email,
        @NotBlank
        @NotNull
        String firstName,
        @NotBlank
        @NotNull
        String lastName
) {
}
