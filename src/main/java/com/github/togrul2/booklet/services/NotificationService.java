package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.clients.NotificationWebClient;
import com.github.togrul2.booklet.events.ReservationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Service responsible for sending notifications to the notification service.
 * Methods are invoked after the originating transaction has been committed,
 * ensuring notifications are never sent for rolled-back operations.
 */
@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class NotificationService {
    private NotificationWebClient notificationWebClient;

    /**
     * Sends a notification after a book reservation transaction is committed.
     *
     * @param event the event carrying the newly created reservation details
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationCreated(ReservationCreatedEvent event) {
        long userID = event.reservation().user().id();
        String message = String.format(
                "Your reservation for book '%s' from '%s' until '%s' has been created successfully.",
                event.reservation().book().title(),
                event.reservation().startDate(),
                event.reservation().endDate()
        );
        log.info(
                "Sending reservation notification: reservationId={}, bookId={}, userId={}",
                event.reservation().id(),
                event.reservation().book().id(),
                event.reservation().user().id()
        );
        notificationWebClient.sendNotification(userID, message);
    }
}

