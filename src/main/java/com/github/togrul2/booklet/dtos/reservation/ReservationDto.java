package com.github.togrul2.booklet.dtos.reservation;

import com.github.togrul2.booklet.dtos.book.BookDto;
import com.github.togrul2.booklet.dtos.user.UserDto;

import java.time.LocalDateTime;

public record ReservationDto(
        long id,
        UserDto user,
        BookDto book,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
