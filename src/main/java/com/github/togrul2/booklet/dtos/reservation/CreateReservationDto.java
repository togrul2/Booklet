package com.github.togrul2.booklet.dtos.reservation;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReservationDto(
        @NotNull
        long bookId,
        @NotNull
        LocalDateTime startDate,
        @NotNull
        LocalDateTime endDate
) {
}
