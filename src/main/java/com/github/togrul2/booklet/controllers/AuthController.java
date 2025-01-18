package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.auth.LoginDto;
import com.github.togrul2.booklet.dtos.auth.RefreshRequestDto;
import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.services.AuthService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Auth")
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@ApiResponses(
        value = {
                @ApiResponse(responseCode = "400", description = "Invalid input"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
)
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

    @PostMapping("/logout")
    public void logout(@RequestBody @Valid RefreshRequestDto refreshRequestDto) {
        authService.logout(refreshRequestDto);
    }
}
