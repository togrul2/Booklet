package com.github.togrul2.booklet.dtos.user;

import com.github.togrul2.booklet.entities.Role;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public record UserFilterDto(
        String firstName,
        String lastName,
        String email,
        Boolean active,
        LocalDateTime minCreationDatetime,
        LocalDateTime maxCreationDatetime,
        Role role
) implements Serializable {
}
