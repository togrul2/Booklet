package com.github.togrul2.booklet.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserDto(
        @Email(groups = {UpdateUser.class, CreateUser.class})
        String email,
        @NotBlank(groups = CreateUser.class)
        @NotNull(groups = CreateUser.class)
        String firstName,
        @NotBlank(groups = CreateUser.class)
        @NotNull(groups = CreateUser.class)
        String lastName
) {
}
