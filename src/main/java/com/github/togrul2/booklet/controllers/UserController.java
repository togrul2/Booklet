package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.dtos.reservation.ReservationDto;
import com.github.togrul2.booklet.dtos.user.CreateUserDto;
import com.github.togrul2.booklet.dtos.user.PartialUpdateUserDto;
import com.github.togrul2.booklet.dtos.user.UpdateUserDto;
import com.github.togrul2.booklet.dtos.user.UserDto;
import com.github.togrul2.booklet.services.AuthService;
import com.github.togrul2.booklet.services.ReservationService;
import com.github.togrul2.booklet.services.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Tag(name = "Users")
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    private final ReservationService reservationService;

    @GetMapping
    public Page<UserDto> getAll(@ParameterObject Pageable pageable) {
        return userService.findAll(pageable);
    }

    @PostMapping
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "409", description = "Conflict with current data", content = {
                    @Content(mediaType = "application/json")
            })
    })
    public ResponseEntity<TokenPairDto> register(@RequestBody @Valid CreateUserDto createUserDto) {
        UserDto user = userService.register(createUserDto);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.id())
                .toUri();

        return ResponseEntity
                .created(uri)
                .body(authService.createTokenPairs(user.email()));
    }

    @GetMapping("/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(mediaType = "application/json")
        ),
    })
    public UserDto getById(@PathVariable long id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User replaced"),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(mediaType = "application/json")
        ),
    })
    public UserDto replace(@PathVariable long id, @RequestBody @Valid UpdateUserDto updateUserDto) {
        return userService.replace(id, updateUserDto);
    }

    @PatchMapping("/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated"),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict with current data",
            content = @Content(mediaType = "application/json")
        )
    })
    public UserDto update(@PathVariable long id, @RequestBody @Valid PartialUpdateUserDto partialUpdateUserDto) {
        return userService.update(id, partialUpdateUserDto);
    }

    @DeleteMapping("/{id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted"),
        @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(mediaType = "application/json")
        ),
    })
    public void delete(@PathVariable long id) {
        userService.delete(id);
    }

    @GetMapping("/me")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auth user found"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Auth user not found",
                    content = @Content(mediaType = "application/json")
            ),
        }
    )
    public UserDto getAuthUser() {
        return userService.findAuthUser();
    }

    @PutMapping("/me")
    public UserDto replaceAuthUser(@RequestBody @Valid UpdateUserDto updateUserDto) {
        return userService.replaceAuthUser(updateUserDto);
    }

    @PatchMapping("/me")
    public UserDto updateAuthUser(@RequestBody @Valid PartialUpdateUserDto partialUpdateUserDto) {
        return userService.updateAuthUser(partialUpdateUserDto);
    }

    @DeleteMapping("/me")
    public void deleteAuthUser() {
        userService.deleteAuthUser();
    }

    @GetMapping("/me/reservations")
    public Page<ReservationDto> getAuthUserReservations(@ParameterObject Pageable pageable) {
        return reservationService.findAllForAuthUser(pageable);
    }

    @GetMapping("/me/reservations/{id}")
    public ReservationDto findReservationForAuthUserById(@PathVariable long id) {
        return reservationService.findByIdForAuthUser(id);
    }
}
