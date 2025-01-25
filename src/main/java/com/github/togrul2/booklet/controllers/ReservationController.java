package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.annotations.ApiErrorResponses;
import com.github.togrul2.booklet.dtos.reservation.CreateReservationDto;
import com.github.togrul2.booklet.dtos.reservation.ReservationDto;
import com.github.togrul2.booklet.dtos.reservation.UpdateReservationDto;
import com.github.togrul2.booklet.services.ReservationService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@ApiErrorResponses
@RequiredArgsConstructor
@Tag(name = "Reservations")
@RequestMapping("/api/v1/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping
    @Cacheable(value = "reservations", key = "#pageable")
    @ApiResponse(responseCode = "200", description = "Ok")
    public Page<ReservationDto> findAll(@ParameterObject Pageable pageable) {
        return reservationService.findAll(pageable);
    }

    @PostMapping
    @CacheEvict(value = {"reservations", "authUserReservations"}, allEntries = true)
    @ApiResponse(responseCode = "201", description = "Created")
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
    @Cacheable(value = "reservation", key = "#id")
    @ApiResponse(responseCode = "200", description = "Ok")
    public ReservationDto findById(@PathVariable long id) {
        return reservationService.findById(id);
    }

    @PutMapping("/{id}")
    @Caching(
            put = @CachePut(value = "reservation", key = "#id"),
            evict = @CacheEvict(
                    value = {"reservations", "authUserReservations", "authUserReservation"},
                    allEntries = true
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDto.class))
    )
    public ReservationDto replace(
            @PathVariable long id, @RequestBody @Valid CreateReservationDto requestBody
    ) {
        return reservationService.replace(id, requestBody);
    }

    @PatchMapping("/{id}")
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDto.class))
    )
    @Caching(
            put = @CachePut(value = "reservation", key = "#id"),
            evict = @CacheEvict(
                    value = {"reservations", "authUserReservations", "authUserReservation"},
                    allEntries = true
            )
    )
    public ReservationDto update(
            @PathVariable long id, @RequestBody @Valid UpdateReservationDto requestBody
    ) {
        return reservationService.update(id, requestBody);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "No Content")
    @Caching(
            evict = {
                    @CacheEvict(value = "reservation", key = "#id"),
                    @CacheEvict(
                            value = {"reservations", "authUserReservations", "authUserReservation"},
                            allEntries = true
                    )
            }
    )
    public ResponseEntity<Void> delete(@PathVariable long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
