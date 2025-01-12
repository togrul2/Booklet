package com.github.togrul2.booklet.dtos.reservation;

import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record UpdateReservationDto(@Positive Long bookId, LocalDateTime startDate, LocalDateTime endDate) {
}
