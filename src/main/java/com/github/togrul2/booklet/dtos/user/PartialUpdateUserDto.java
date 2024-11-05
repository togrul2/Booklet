package com.github.togrul2.booklet.dtos.user;

import jakarta.validation.constraints.Email;

public record PartialUpdateUserDto(
        @Email
        String email,
        String firstName,
        String lastName
) {
}
