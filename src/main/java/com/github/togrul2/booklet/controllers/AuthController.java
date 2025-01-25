package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.annotations.ApiErrorResponses;
import com.github.togrul2.booklet.dtos.auth.LoginDto;
import com.github.togrul2.booklet.dtos.auth.RefreshRequestDto;
import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.services.AuthService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@ApiErrorResponses
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TokenPairDto.class)
            )
    )
    public TokenPairDto login(@RequestBody @Valid LoginDto loginDto) {
        return authService.login(loginDto);
    }

    @PostMapping("/refresh")
    @ApiResponse(
            responseCode = "200",
            description = "Ok",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TokenPairDto.class)
            )
    )
    public TokenPairDto refresh(@RequestBody @Valid RefreshRequestDto refreshToken) {
        return authService.refresh(refreshToken);
    }

    @PostMapping("/validate")
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = "application/json"))
    public void validate(@RequestBody @Valid RefreshRequestDto refreshRequestDto) {
        authService.validate(refreshRequestDto);
    }

    @PostMapping("/logout")
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = "application/json"))
    public void logout(@RequestBody @Valid RefreshRequestDto refreshRequestDto) {
        authService.logout(refreshRequestDto);
    }
}
