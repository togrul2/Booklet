package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.auth.LoginDto;
import com.github.togrul2.booklet.dtos.auth.RefreshRequestDto;
import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.entities.Token;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.repositories.TokenRepository;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    public TokenPairDto login(@NonNull LoginDto loginDto) {
        User user = (User) userDetailsService.loadUserByUsername(loginDto.email());

        if (!passwordEncoder.matches(loginDto.password(), user.getPassword())) {
            throw new UsernameNotFoundException("User not found");
        }

        Token refreshToken = Token
                .builder()
                .token(jwtService.createRefreshToken(user, user.getRole()))
                .user(user)
                .active(true)
                .build();
        tokenRepository.save(refreshToken);

        return TokenPairDto
                .builder()
                .accessToken(jwtService.createAccessToken(user, user.getRole()))
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public TokenPairDto refresh(@NonNull RefreshRequestDto refreshTokenDto) {
        validate(refreshTokenDto);
        final String refreshToken = refreshTokenDto.refreshToken();

        User user = (User) userDetailsService.loadUserByUsername(
                jwtService.extractUsername(refreshToken)
        );

        return TokenPairDto
                .builder()
                .accessToken(jwtService.createAccessToken(user, user.getRole()))
                .refreshToken(jwtService.createRefreshToken(user, user.getRole()))
                .build();
    }

    public void validate(@NonNull RefreshRequestDto refreshRequestDto) {
        final String refreshToken = refreshRequestDto.refreshToken();
        final boolean isTokenActive = tokenRepository
                .findByToken(refreshToken)
                .map(Token::isActive)
                .orElse(false);
        if (!jwtService.isRefreshToken(refreshToken) || !isTokenActive) {
            throw new JwtException("Bad refresh token");
        }
    }

    public TokenPairDto createTokenPairs(String email) {
        User user = (User) userDetailsService.loadUserByUsername(email);

        Token refreshToken = Token
                .builder()
                .token(jwtService.createRefreshToken(user, user.getRole()))
                .user(user)
                .active(true)
                .build();

        return TokenPairDto
                .builder()
                .accessToken(jwtService.createAccessToken(user, user.getRole()))
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public void logout(@NonNull RefreshRequestDto refreshRequestDto) {
        String refreshToken = refreshRequestDto.refreshToken();
        tokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setActive(false);
                    tokenRepository.save(token);
                });
    }
}
