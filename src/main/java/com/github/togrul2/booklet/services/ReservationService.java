package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.reservation.CreateReservationDto;
import com.github.togrul2.booklet.dtos.reservation.ReservationDto;
import com.github.togrul2.booklet.dtos.reservation.UpdateReservationDto;
import com.github.togrul2.booklet.entities.Reservation;
import com.github.togrul2.booklet.exceptions.BookNotFound;
import com.github.togrul2.booklet.exceptions.ReservationNotFound;
import com.github.togrul2.booklet.exceptions.UserNotFound;
import com.github.togrul2.booklet.mappers.ReservationMapper;
import com.github.togrul2.booklet.repositories.BookRepository;
import com.github.togrul2.booklet.repositories.ReservationRepository;
import com.github.togrul2.booklet.repositories.UserRepository;
import com.github.togrul2.booklet.security.annotations.IsAdmin;
import com.github.togrul2.booklet.security.annotations.IsUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @PostAuthorize(
            "hasRole('USER') && (hasRole('ADMIN') || returnObject.user.email == authentication.principal.username)"
    )
    public ReservationDto findById(long id) {
        return reservationRepository
                .findById(id)
                .map(ReservationMapper.INSTANCE::toReservationDto)
                .orElseThrow(RuntimeException::new);
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
                .orElseThrow(RuntimeException::new);
    }

    /**
     * Validates reservation dates. Checks if the start date is before the end date. Also checks if the start date and
     * end date are in the future.
     * @param reservation Reservation entity instance to validate.
     * @throws IllegalArgumentException If the reservation dates are invalid.
     */
    private void validateReservation(@NonNull Reservation reservation) {
        Duration reservationDuration = Duration.between(reservation.getStartDate(), reservation.getEndDate());
        if (LocalDateTime.now().isAfter(reservation.getStartDate()) ||
            LocalDateTime.now().isAfter(reservation.getEndDate()) ||
            // TODO: Add minimum reservation duration check.
            reservationDuration.isNegative()) {
            throw new IllegalArgumentException("Invalid reservation dates.");
        }

        if (reservationRepository.hasOverlappingSession(
                reservation.getBook(),
                reservation.getStartDate(),
                reservation.getEndDate()
        )) {
            throw new IllegalArgumentException("Overlapping reservation is not allowed.");
        }
    }

    @IsAdmin
    public ReservationDto reserveBook(@NonNull UserDetails userDetails, @NonNull CreateReservationDto reservationDto) {
        // Create reservation instance.
        Reservation reservation = ReservationMapper.INSTANCE.toReservation(reservationDto);
        reservation.setUser(userRepository.findByEmail(userDetails.getUsername()).orElseThrow(UserNotFound::new));
        reservation.setBook(bookRepository.findById(reservationDto.bookId()).orElseThrow(BookNotFound::new));

        validateReservation(reservation);
        return ReservationMapper.INSTANCE.toReservationDto(reservationRepository.save(reservation));
    }

    @IsAdmin
    public ReservationDto replace(long id, @NonNull CreateReservationDto requestBody) {
        // Check if the reservation exists.
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFound();
        }

        // Create reservation instance.
        Reservation reservation = ReservationMapper.INSTANCE.toReservation(requestBody);
        reservation.setBook(bookRepository.findById(requestBody.bookId()).orElseThrow(BookNotFound::new));

        validateReservation(reservation);
        return ReservationMapper.INSTANCE.toReservationDto(reservationRepository.save(reservation));
    }

    @IsAdmin
    public ReservationDto update(long id, @NonNull UpdateReservationDto requestBody) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(ReservationNotFound::new);

        // Update reservation fields if provided.
        if (!Objects.isNull(requestBody.startDate())) {
            reservation.setStartDate(requestBody.startDate());
        }

        if (!Objects.isNull(requestBody.endDate())) {
            reservation.setEndDate(requestBody.endDate());
        }

        if (!Objects.isNull(requestBody.bookId())) {
            reservation.setBook(bookRepository.findById(requestBody.bookId()).orElseThrow(BookNotFound::new));
        }

        validateReservation(reservation);
        return ReservationMapper.INSTANCE.toReservationDto(reservationRepository.save(reservation));
    }

    /**
     * Deletes a reservation. This method validates the user's permission to delete the reservation.
     * If user is not an admin, the reservation must belong to the authenticated user.
     * @param id The id of the reservation to delete.
     * @throws ReservationNotFound If the reservation does not exist.
     */
    public void delete(long id) {
        ReservationDto reservation = findById(id);
        reservationRepository.deleteById(reservation.id());
    }
}
