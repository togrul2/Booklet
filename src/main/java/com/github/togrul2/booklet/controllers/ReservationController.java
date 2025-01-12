package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.reservation.CreateReservationDto;
import com.github.togrul2.booklet.dtos.reservation.ReservationDto;
import com.github.togrul2.booklet.dtos.reservation.UpdateReservationDto;
import com.github.togrul2.booklet.services.ReservationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reservations")
@RequestMapping("/api/v1/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping
    public Page<ReservationDto> findAll(@ParameterObject Pageable pageable) {
        return reservationService.findAll(pageable);
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody @Valid CreateReservationDto requestBody,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReservationDto reservationDto = reservationService.reserveBook(userDetails, requestBody);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/reservations/{id}")
                .buildAndExpand(reservationDto.id())
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    public ReservationDto findById(@PathVariable long id) {
        return reservationService.findById(id);
    }

    @PutMapping("/{id}")
    public ReservationDto replace(
            @PathVariable long id, @RequestBody @Valid CreateReservationDto requestBody
    ) {
        return reservationService.replace(id, requestBody);
    }

    @PatchMapping("/{id}")
    public ReservationDto update(
            @PathVariable long id, @RequestBody @Valid UpdateReservationDto requestBody
    ) {
        return reservationService.update(id, requestBody);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
