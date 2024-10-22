package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.auth.LoginDto;
import com.github.togrul2.booklet.dtos.auth.RefreshRequestDto;
import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.entities.Token;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.repositories.TokenRepository;
import com.github.togrul2.booklet.repositories.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    public TokenPairDto login(@NonNull LoginDto loginDto) {
        User user = userRepository
                .findByEmail(loginDto.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(loginDto.password(), user.getPassword())) {
            throw new UsernameNotFoundException("User not found");
        }

        Token refreshToken = Token
                .builder()
                .token(jwtService.createRefreshToken(user))
                .user(user)
                .active(true)
                .build();
        tokenRepository.save(refreshToken);

        return TokenPairDto
                .builder()
                .accessToken(jwtService.createAccessToken(user))
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public TokenPairDto refresh(@NonNull RefreshRequestDto refreshTokenDto) {
        boolean isTokenActive = tokenRepository.isTokenActive(refreshTokenDto.refreshToken());
        if (jwtService.isRefreshTokenValid(refreshTokenDto.refreshToken()) && !isTokenActive) {
            throw new JwtException("Bad refresh token");
        }

        String email = jwtService.extractUsername(refreshTokenDto.refreshToken());
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return TokenPairDto
                .builder()
                .accessToken(jwtService.createAccessToken(user))
                .refreshToken(jwtService.createRefreshToken(user))
                .build();
    }

    public void validate(@NonNull RefreshRequestDto refreshRequestDto) {
        final String refreshToken = refreshRequestDto.refreshToken();
        if (
                jwtService.isRefreshTokenValid(refreshToken) ||
                        !tokenRepository.isTokenActive(refreshToken)
        ) {
            throw new JwtException("Bad refresh token");
        }
    }

    public TokenPairDto createTokenPairs(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Token refreshToken = Token
                .builder()
                .token(jwtService.createRefreshToken(user))
                .user(user)
                .active(true)
                .build();

        return TokenPairDto
                .builder()
                .accessToken(jwtService.createAccessToken(user))
                .refreshToken(refreshToken.getToken())
                .build();
    }
}
