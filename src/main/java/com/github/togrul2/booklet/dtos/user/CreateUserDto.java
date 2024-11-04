package com.github.togrul2.booklet.dtos.user;

import com.github.togrul2.booklet.entities.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record CreateUserDto(
        @Email
        String email,
        @Pattern(regexp = User.PASSWORD_PATTERN)
        String password,
        @NotBlank
        @NotNull
        String firstName,
        @NotBlank
        @NotNull
        String lastName
) {}
