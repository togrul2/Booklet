package com.github.togrul2.booklet.repositories;

import com.github.togrul2.booklet.entities.Book;
import com.github.togrul2.booklet.entities.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    /**
     * Returns a page of reservation belonging to the authenticated user.
     *
     * @param pageable the pageable to request a paged result, can be {@link Pageable#unpaged()}, must not be
     *          {@literal null}.
     * @return a page of reservations.
     */
    @Query(
            "SELECT r FROM Reservation r " +
            "WHERE r.user.email = ?#{principal?.username}"
    )
    Page<Reservation> findAllForAuthUser(Pageable pageable);

    @Query(
            "SELECT CASE " +
            "WHEN COUNT(r) > 0 " +
            "THEN TRUE " +
            "ELSE FALSE " +
            "END " +
            "FROM Reservation r " +
            "WHERE r.book = :book " +
            "AND ((r.startDate <= :start AND r.endDate >= :start) " +
            "OR (r.startDate <= :end AND r.endDate >= :end))"
    )
    boolean hasOverlappingSession(Book book, LocalDateTime start, LocalDateTime end);

    @Query(
            "SELECT r FROM Reservation r " +
            "WHERE r.id = :id " +
            "AND r.user.email = ?#{principal?.username}" +
            "OR ?#{principal?.authorities.contains('ROLE_ADMIN')} = true"
    )
    Optional<Reservation> findById(long id);

    @Query(
            "SELECT r FROM Reservation r " +
            "WHERE r.id = :id " +
            "AND r.user.email = ?#{principal?.username}"
    )
    Optional<Reservation> findByIdForAuthUser(long id);
}
