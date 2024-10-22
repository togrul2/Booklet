package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.auth.LoginDto;
import com.github.togrul2.booklet.dtos.auth.RefreshRequestDto;
import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.services.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public TokenPairDto login(@RequestBody @Valid LoginDto loginDto) {
        return authService.login(loginDto);
    }

    @PostMapping("/refresh")
    public TokenPairDto refresh(@RequestBody @Valid RefreshRequestDto refreshToken) {
        return authService.refresh(refreshToken);
    }

    @PostMapping("/validate")
    public void validate(@RequestBody @Valid RefreshRequestDto refreshRequestDto) {
        authService.validate(refreshRequestDto);
    }
}
