package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.annotations.ApiErrorResponses;
import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.dtos.reservation.ReservationDto;
import com.github.togrul2.booklet.dtos.user.*;
import com.github.togrul2.booklet.services.AuthService;
import com.github.togrul2.booklet.services.ReservationService;
import com.github.togrul2.booklet.services.UserService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@ApiErrorResponses
@Tag(name = "Users")
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    private final ReservationService reservationService;

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Ok")
    @Cacheable(value = "users", key = "#pageable + ';' + #filterDto")
    public Page<UserDto> getAll(@ParameterObject Pageable pageable, @ParameterObject UserFilterDto filterDto) {
        return userService.findAll(pageable, filterDto);
    }

    @PostMapping
    @CacheEvict(value = "users", allEntries = true)
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json"))
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
    @Cacheable(value = "user", key = "#id")
    @ApiResponse(responseCode = "200", description = "Ok")
    public UserDto getById(@PathVariable long id) {
        return userService.findById(id);
    }

    @PatchMapping("/{id}")
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
    )
    @Caching(
            put = @CachePut(value = "user", key = "#id"),
            evict = @CacheEvict(value = {"users", "authUser"}, allEntries = true)
    )
    public UserDto update(
            @PathVariable long id, @RequestBody @Validated(UpdateUser.class) UpdateUserDto updateUserDto
    ) {
        return userService.update(id, updateUserDto);
    }

    @PutMapping("/{id}")
    @Caching(
            put = @CachePut(value = "user", key = "#id"),
            evict = @CacheEvict(value = {"users", "authUser"}, allEntries = true)
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
    )
    public UserDto replace(
            @PathVariable long id, @RequestBody @Validated(CreateUser.class) UpdateUserDto updateUserDto
    ) {
        return userService.update(id, updateUserDto);
    }

    @DeleteMapping("/{id}")
    @Caching(
            evict = {
                    @CacheEvict(value = "user", key = "#id"),
                    @CacheEvict(value = {"users", "authUser"}, allEntries = true)
            }
    )
    @ApiResponse(responseCode = "204", description = "User deleted")
    public void delete(@PathVariable long id) {
        userService.delete(id);
    }

    @GetMapping("/me")
    @ApiResponse(responseCode = "200", description = "Ok")
    @Cacheable(value = "authUser", key = "#principal?.username", condition = "#principal?.username != null")
    public UserDto getAuthUser() {
        return userService.findAuthUser();
    }

    @PutMapping("/me")
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
    )
    @Caching(
            put = @CachePut(
                    value = "authUser", key = "#principal?.username", condition = "#principal?.username != null"
            ),
            evict = @CacheEvict(value = {"users", "user"}, allEntries = true)
    )
    public UserDto replaceAuthUser(@RequestBody @Validated(CreateUser.class) UpdateUserDto updateUserDto) {
        return userService.updateAuthUser(updateUserDto);
    }

    @PatchMapping("/me")
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
    )
    @Caching(
            put = @CachePut(
                    value = "authUser", key = "#principal.username", condition = "#principal?.username != null"
            ),
            evict = @CacheEvict(value = {"users", "user"}, allEntries = true)
    )
    public UserDto updateAuthUser(@RequestBody @Validated(UpdateUser.class) UpdateUserDto updateUserDto) {
        return userService.updateAuthUser(updateUserDto);
    }

    @DeleteMapping("/me")
    @ApiResponse(responseCode = "204", description = "No content")
    @Caching(
            evict = {
                    @CacheEvict(value = {"users", "user"}, allEntries = true),
                    @CacheEvict(
                            value = "authUser",
                            key = "#principal?.username",
                            condition = "#principal?.username != null"
                    )
            }
    )
    public void deleteAuthUser() {
        userService.deleteAuthUser();
    }

    @GetMapping("/me/reservations")
    @ApiResponse(responseCode = "200", description = "Ok")
    @Cacheable(
            value = "authUserReservations",
            key = "#principal?.username + ';' + #pageable",
            condition = "#principal?.username != null"
    )
    public Page<ReservationDto> getAuthUserReservations(@ParameterObject Pageable pageable) {
        return reservationService.findAllForAuthUser(pageable);
    }

    @GetMapping("/me/reservations/{id}")
    @ApiResponse(responseCode = "200", description = "Ok")
    @Cacheable(
            value = "authUserReservation",
            key = "#principal?.username + ';' + #id",
            condition = "#principal.username != null"
    )
    public ReservationDto findReservationForAuthUserById(@PathVariable long id) {
        return reservationService.findByIdForAuthUser(id);
    }
}
