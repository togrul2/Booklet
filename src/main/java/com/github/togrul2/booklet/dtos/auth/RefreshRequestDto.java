package com.github.togrul2.booklet.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshRequestDto(@NotNull @NotBlank String refreshToken) {
}
