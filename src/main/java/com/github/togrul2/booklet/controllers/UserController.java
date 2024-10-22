package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.PaginationDto;
import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.dtos.user.CreateUserDto;
import com.github.togrul2.booklet.dtos.user.UserDto;
import com.github.togrul2.booklet.services.AuthService;
import com.github.togrul2.booklet.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public Page<UserDto> getAll(@Valid PaginationDto paginationDto) {
        return userService.findAll(paginationDto.getPageNumber(), paginationDto.getPageSize());
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable long id) {
        return userService.findById(id);
    }

    @PostMapping
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
    public UserDto getAuthUser(@AuthenticationPrincipal Long userId) {
        return userService.findById(userId);
    }
}
