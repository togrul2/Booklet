package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.reservation.CreateReservationDto;
import com.github.togrul2.booklet.dtos.reservation.ReservationDto;
import com.github.togrul2.booklet.dtos.reservation.UpdateReservationDto;
import com.github.togrul2.booklet.entities.Book;
import com.github.togrul2.booklet.entities.Reservation;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.mappers.ReservationMapper;
import com.github.togrul2.booklet.repositories.BookRepository;
import com.github.togrul2.booklet.repositories.ReservationRepository;
import com.github.togrul2.booklet.repositories.UserRepository;
import com.github.togrul2.booklet.annotations.IsAdmin;
import com.github.togrul2.booklet.annotations.IsUser;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

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
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found."));
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
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found."));
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
    public ReservationDto reserveBook(@NonNull UserDetails userDetails, @NonNull CreateReservationDto reservationDto) {
        // Create reservation instance.
        Reservation reservation = ReservationMapper.INSTANCE.toReservation(reservationDto);
        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        Book book = bookRepository
                .findById(reservationDto.bookId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found."));
        reservation.setUser(user);
        reservation.setBook(book);

        validateReservation(reservation);
        return ReservationMapper.INSTANCE.toReservationDto(reservationRepository.save(reservation));
    }

    @IsAdmin
    public ReservationDto replace(long id, @NonNull CreateReservationDto requestBody) {
        // Check if the reservation exists.
        if (!reservationRepository.existsById(id)) {
            throw new EntityNotFoundException("Reservation not found.");
        }

        // Create reservation instance.
        Reservation reservation = ReservationMapper.INSTANCE.toReservation(requestBody);
        Book book = bookRepository
                .findById(requestBody.bookId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found."));
        reservation.setBook(book);

        validateReservation(reservation);
        return ReservationMapper.INSTANCE.toReservationDto(reservationRepository.save(reservation));
    }

    @IsAdmin
    public ReservationDto update(long id, @NonNull UpdateReservationDto requestBody) {
        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found."));

        // Update reservation fields if provided.
        if (!Objects.isNull(requestBody.startDate())) {
            reservation.setStartDate(requestBody.startDate());
        }

        if (!Objects.isNull(requestBody.endDate())) {
            reservation.setEndDate(requestBody.endDate());
        }

        if (!Objects.isNull(requestBody.bookId())) {
            Book book = bookRepository
                    .findById(requestBody.bookId())
                    .orElseThrow(() -> new EntityNotFoundException("Book not found."));
            reservation.setBook(book);
        }

        validateReservation(reservation);
        return ReservationMapper.INSTANCE.toReservationDto(reservationRepository.save(reservation));
    }

    /**
     * Deletes a reservation. This method validates the user's permission to delete the reservation.
     * If user is not an admin, the reservation must belong to the authenticated user.
     *
     * @param id The id of the reservation to delete.
     * @throws EntityNotFoundException the reservation does not exist.
     */
    @IsUser
    public void delete(long id) {
        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found."));
        reservationRepository.delete(reservation);
    }
}
