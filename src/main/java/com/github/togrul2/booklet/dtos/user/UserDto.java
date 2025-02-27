package com.github.togrul2.booklet.dtos.user;

import com.github.togrul2.booklet.entities.Role;

import java.io.Serializable;

public record UserDto(
        long id,
        String email,
        String firstName,
        String lastName,
        boolean active,
        String creationDate,
        String modificationDate,
        Role role
) implements Serializable {
}
