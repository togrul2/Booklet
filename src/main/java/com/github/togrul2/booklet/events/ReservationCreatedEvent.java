package com.github.togrul2.booklet.events;

import com.github.togrul2.booklet.dtos.reservation.ReservationDto;

/**
 * Application event published after a book reservation is successfully persisted.
 * Listeners should use {@link org.springframework.transaction.event.TransactionalEventListener}
 * with {@code phase = AFTER_COMMIT} so the notification is only sent once the
 * transaction has been durably committed.
 *
 * @param reservation the DTO of the newly created reservation
 */
public record ReservationCreatedEvent(ReservationDto reservation) {
}

