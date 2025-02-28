package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.annotations.IsAdmin;
import com.github.togrul2.booklet.annotations.IsUser;
import com.github.togrul2.booklet.dtos.reservation.ReservationRequestDto;
import com.github.togrul2.booklet.dtos.reservation.ReservationDto;
import com.github.togrul2.booklet.entities.Book;
import com.github.togrul2.booklet.entities.Reservation;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.mappers.ReservationMapper;
import com.github.togrul2.booklet.repositories.BookRepository;
import com.github.togrul2.booklet.repositories.ReservationRepository;
import com.github.togrul2.booklet.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {
    private static final Duration MINIMUM_RESERVATION_DURATION = Duration.ofDays(1);
    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @IsUser
    public ReservationDto findById(long id) {
        return reservationRepository
                .findById(id)
                .map(ReservationMapper.INSTANCE::toReservationDto)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found."));
    }

    @IsAdmin
    public Page<ReservationDto> findAll(Pageable pageable) {
        return reservationRepository
                .findAll(pageable)
                .map(ReservationMapper.INSTANCE::toReservationDto);
    }

    @IsUser
    public Page<ReservationDto> findAllForAuthUser(Pageable pageable) {
        return reservationRepository
                .findAllForAuthUser(pageable)
                .map(ReservationMapper.INSTANCE::toReservationDto);
    }

    @IsUser
    public ReservationDto findByIdForAuthUser(long id) {
        return reservationRepository
                .findByIdForAuthUser(id)
                .map(ReservationMapper.INSTANCE::toReservationDto)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found."));
    }

    /**
     * Validates reservation dates. Checks if the start date is before the end date. Also checks if the start date and
     * end date are in the future.
     *
     * @param reservation Reservation entity instance to validate.
     * @throws IllegalArgumentException If the reservation dates are invalid.
     */
    private void validateReservation(@NonNull Reservation reservation) {
        Duration reservationDuration = Duration.between(reservation.getStartDate(), reservation.getEndDate());
        if (LocalDateTime.now().isAfter(reservation.getStartDate()) ||
                LocalDateTime.now().isAfter(reservation.getEndDate()) ||
                reservation.getStartDate().isAfter(reservation.getEndDate())) {
            throw new IllegalArgumentException("Invalid reservation dates.");
        }

        if (reservationDuration.compareTo(MINIMUM_RESERVATION_DURATION) < 0) {
            throw new IllegalArgumentException("Minimum reservation duration is 1 day.");
        }

        if (reservationRepository.hasOverlappingSession(
                reservation.getBook(),
                reservation.getStartDate(),
                reservation.getEndDate()
        )) {
            throw new IllegalArgumentException("Overlapping reservation session.");
        }
    }

    @IsUser
    public ReservationDto reserveBook(@NonNull UserDetails userDetails, @NonNull ReservationRequestDto reservationDto) {
        // Create reservation instance.
        Reservation reservation = ReservationMapper.INSTANCE.toReservation(reservationDto);
        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        Book book = bookRepository
                .findById(reservationDto.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found."));
        reservation.setUser(user);
        reservation.setBook(book);

        validateReservation(reservation);
        return ReservationMapper.INSTANCE.toReservationDto(reservationRepository.save(reservation));
    }

    @IsAdmin
    public ReservationDto update(long id, ReservationRequestDto requestBody) {
        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found."));

        // Update reservation fields if provided.
        Optional.ofNullable(requestBody.startDate()).ifPresent(reservation::setStartDate);
        Optional.ofNullable(requestBody.endDate()).ifPresent(reservation::setEndDate);
        Optional
                .ofNullable(requestBody.bookId())
                .ifPresent(bookId -> {
                    Book book = bookRepository
                            .findById(bookId)
                            .orElseThrow(() -> new ResourceNotFoundException("Book not found."));
                    reservation.setBook(book);
                });

        validateReservation(reservation);
        return ReservationMapper.INSTANCE.toReservationDto(reservationRepository.save(reservation));
    }

    /**
     * Deletes a reservation. This method validates the user's permission to delete the reservation.
     * If user is not an admin, the reservation must belong to the authenticated user.
     *
     * @param id The id of the reservation to delete.
     * @throws ResourceNotFoundException() the reservation does not exist.
     */
    @IsUser
    public void delete(long id) {
        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found."));
        reservationRepository.delete(reservation);
    }
}
