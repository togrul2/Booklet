package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.dtos.user.CreateUserDto;
import com.github.togrul2.booklet.dtos.user.PartialUpdateUserDto;
import com.github.togrul2.booklet.dtos.user.UpdateUserDto;
import com.github.togrul2.booklet.dtos.user.UserDto;
import com.github.togrul2.booklet.services.AuthService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDto> getAll(@ParameterObject Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{idOrEmail}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
    })
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto getById(@PathVariable String idOrEmail) {
        // TODO: think of something better.
        try {
            return userService.findById(Long.parseLong(idOrEmail));
        } catch (NumberFormatException e) {
            return userService.findByEmail(idOrEmail);
        }
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

    @GetMapping("/me")
    public UserDto getAuthUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @PutMapping("/me")
    public UserDto replaceAuthUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UpdateUserDto updateUserDto
    ) {
        return userService.replace(userDetails.getUsername(), updateUserDto);
    }

    @PatchMapping("/me")
    public UserDto updateAuthUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid PartialUpdateUserDto partialUpdateUserDto
    ) {
        return userService.update(userDetails.getUsername(), partialUpdateUserDto);
    }

    @DeleteMapping("/me")
    public void deleteAuthUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.delete(userDetails.getUsername());
    }
}
