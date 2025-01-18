package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.auth.LoginDto;
import com.github.togrul2.booklet.dtos.auth.RefreshRequestDto;
import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.entities.Token;
import com.github.togrul2.booklet.entities.User;
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

    public TokenPairDto login(LoginDto loginDto) {
        User user = (User) userDetailsService.loadUserByUsername(loginDto.email());

        if (!passwordEncoder.matches(loginDto.password(), user.getPassword())) {
            throw new UsernameNotFoundException("User not found");
        }

        Token refreshToken = jwtService.createAndStoreRefreshToken(user, user.getRole());
        return TokenPairDto
                .builder()
                .accessToken(jwtService.createAccessToken(user, user.getRole()))
                .refreshToken(refreshToken.getToken())
                .build();
    }

    /**
     * Creates new token pairs for the user. Used refresh token is deactivated and cannot be used again.
     *
     * @param refreshTokenDto Dto containing refresh token.
     * @return TokenPairDto containing new access and refresh tokens.
     */
    public TokenPairDto refresh(RefreshRequestDto refreshTokenDto) {
        final String refreshToken = refreshTokenDto.refreshToken();

        // Validate refresh token and set it as inactive so it cannot be used anymore.
        jwtService.deactivateRefreshToken(refreshToken);

        User user = (User) userDetailsService.loadUserByUsername(
                jwtService.extractUsername(refreshToken)
        );

        Token newRefreshToken = jwtService.createAndStoreRefreshToken(user, user.getRole());
        return TokenPairDto
                .builder()
                .accessToken(jwtService.createAccessToken(user, user.getRole()))
                .refreshToken(newRefreshToken.getToken())
                .build();
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

    public void logout(RefreshRequestDto refreshRequestDto) {
        jwtService.deactivateRefreshToken(refreshRequestDto.refreshToken());
    }

    public void validate(RefreshRequestDto refreshRequestDto) {
        jwtService.validateRefreshToken(refreshRequestDto.refreshToken());
    }
}
