package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.reservation.ReservationRequestDto;
import com.github.togrul2.booklet.dtos.reservation.ReservationDto;
import com.github.togrul2.booklet.entities.Book;
import com.github.togrul2.booklet.entities.Reservation;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.repositories.BookRepository;
import com.github.togrul2.booklet.repositories.ReservationRepository;
import com.github.togrul2.booklet.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;


@ExtendWith(SpringExtension.class)
public class ReservationServiceTests {
    @InjectMocks
    private ReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetails userDetails;
    private Reservation reservation;
    private User user;
    private Book book;
    private ReservationRequestDto reservationRequestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");

        book = new Book();
        book.setId(1L);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStartDate(LocalDateTime.now().plusDays(1));
        reservation.setEndDate(LocalDateTime.now().plusDays(2));

        reservationRequestDto = new ReservationRequestDto(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
    }

    @Test
    void testFindById() {
        Mockito.when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationDto result = reservationService.findById(1L);

        Assertions.assertNotNull(result);
        Mockito.verify(reservationRepository).findById(1L);
    }

    @Test
    void testFindByIdThrowsEntityNotFoundException() {
        Mockito.when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> reservationService.findById(1L));
        Mockito.verify(reservationRepository).findById(1L);
    }

    @Test
    void testReserveBook() {
        Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(book));
        Mockito.when(reservationRepository.save(Mockito.any())).thenReturn(reservation);

        ReservationDto result = reservationService.reserveBook(userDetails, reservationRequestDto);

        Assertions.assertNotNull(result);
        Mockito.verify(userRepository).findByEmail(Mockito.any());
        Mockito.verify(bookRepository).findById(Mockito.anyLong());
        Mockito.verify(reservationRepository).save(Mockito.any());
    }

    @Test
    void testReserveBookUserNotFound() {
        Mockito.when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> reservationService.reserveBook(userDetails,
                reservationRequestDto));

        Mockito.verify(userRepository).findByEmail(userDetails.getUsername());
        Mockito.verify(bookRepository, Mockito.never()).findById(reservationRequestDto.bookId());
        Mockito.verify(reservationRepository, Mockito.never()).save(reservation);
    }

    @Test
    void testReserveBookBookNotFound() {
        Mockito.when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(bookRepository.findById(reservationRequestDto.bookId())).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> reservationService.reserveBook(userDetails,
                reservationRequestDto));

        Mockito.verify(reservationRepository, Mockito.never()).save(reservation);
        Mockito.verify(userRepository).findByEmail(userDetails.getUsername());
        Mockito.verify(bookRepository).findById(reservationRequestDto.bookId());
    }

    @Test
    void testDeleteReservation() {
        Mockito.when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.delete(1L);

        Mockito.verify(reservationRepository).findById(1L);
        Mockito.verify(reservationRepository).delete(reservation);
    }

    @Test
    void delete_ThrowsException_WhenReservationDoesNotExist() {
        Mockito.when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> reservationService.delete(1L));
        Mockito.verify(reservationRepository).findById(1L);
        Mockito.verify(reservationRepository, Mockito.never()).delete(reservation);
    }
}