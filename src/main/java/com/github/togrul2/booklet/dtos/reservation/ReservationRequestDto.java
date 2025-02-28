package com.github.togrul2.booklet.dtos.reservation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record ReservationRequestDto(
        @NotNull(groups = CreateReservation.class)
        @Positive(groups = {CreateReservation.class, UpdateReservation.class})
        Long bookId,
        @NotNull(groups = CreateReservation.class)
        LocalDateTime startDate,
        @NotNull(groups = CreateReservation.class)
        LocalDateTime endDate
) {
}
