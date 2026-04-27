package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.entities.Token;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.repositories.TokenRepository;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class JwtServiceTests {
    @Mock
    private TokenRepository tokenRepository;
    @InjectMocks
    private JwtService jwtService;

    private final String username = "someusername";
    private UserDetails user;
    private String token;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", "bxBWG9rIQcquX1UubhgI5lIqF4B6+N0GBBN5v/eCSss=");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 300000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 86400000L);
        user = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("dummypassword")
                .roles(Role.USER.name())
                .authorities(Role.USER.getAuthorities())
                .build();
        token = jwtService.createAccessToken(user, Role.USER);
    }

    @Test
    public void testExtractUsername() {
        Assertions.assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    public void testExtractRole() {
        Assertions.assertEquals(Role.USER, jwtService.extractRole(token));
    }

    @Test
    public void testExtractUsernameFromExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1L);
        String expiredToken = jwtService.createAccessToken(user, Role.USER);
        Assertions.assertThrows(JwtException.class, () -> jwtService.extractUsername(expiredToken));
    }

    @Test
    public void testIsAccessToken() {
        Assertions.assertTrue(jwtService.isAccessToken(token));
    }

    @Test
    public void testIsAccessTokenWithRefreshToken() {
        String refreshToken = jwtService.createRefreshToken(user, Role.USER);
        Assertions.assertFalse(jwtService.isAccessToken(refreshToken));
    }

    @Test
    public void testIsRefreshToken() {
        String refreshToken = jwtService.createRefreshToken(user, Role.USER);
        Assertions.assertTrue(jwtService.isRefreshToken(refreshToken));
    }

    @Test
    public void testIsRefreshTokenWithAccessToken() {
        Assertions.assertFalse(jwtService.isRefreshToken(token));
    }

    @Test
    public void testCreateAccessToken() {
        String accessToken = jwtService.createAccessToken(user, Role.USER);
        Assertions.assertNotNull(accessToken);
        Assertions.assertTrue(jwtService.isAccessToken(accessToken));
        Assertions.assertEquals(username, jwtService.extractUsername(accessToken));
        Assertions.assertEquals(Role.USER, jwtService.extractRole(accessToken));
    }

    @Test
    public void testCreateRefreshToken() {
        String refreshToken = jwtService.createRefreshToken(user, Role.USER);
        Assertions.assertNotNull(refreshToken);
        Assertions.assertTrue(jwtService.isRefreshToken(refreshToken));
        Assertions.assertEquals(username, jwtService.extractUsername(refreshToken));
    }

    @Test
    public void testCreateAndStoreRefreshToken() {
        User domainUser = User.builder().email(username).password("secret").role(Role.USER).build();
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenAnswer(i -> i.getArgument(0));

        Token stored = jwtService.createAndStoreRefreshToken(domainUser, Role.USER);

        Assertions.assertNotNull(stored.getToken());
        Assertions.assertTrue(stored.isActive());
        Assertions.assertTrue(jwtService.isRefreshToken(stored.getToken()));
        Mockito.verify(tokenRepository).save(Mockito.any(Token.class));
    }

    @Test
    public void testValidateRefreshTokenSuccess() {
        String refreshToken = jwtService.createRefreshToken(user, Role.USER);
        Mockito.when(tokenRepository.findByToken(refreshToken))
                .thenReturn(Optional.of(Token.builder().token(refreshToken).active(true).build()));

        Assertions.assertDoesNotThrow(() -> jwtService.validateRefreshToken(refreshToken));
    }

    @Test
    public void testValidateRefreshTokenNotFound() {
        String refreshToken = jwtService.createRefreshToken(user, Role.USER);
        Mockito.when(tokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        Assertions.assertThrows(JwtException.class, () -> jwtService.validateRefreshToken(refreshToken));
    }

    @Test
    public void testValidateRefreshTokenInactive() {
        String refreshToken = jwtService.createRefreshToken(user, Role.USER);
        Mockito.when(tokenRepository.findByToken(refreshToken))
                .thenReturn(Optional.of(Token.builder().token(refreshToken).active(false).build()));

        Assertions.assertThrows(JwtException.class, () -> jwtService.validateRefreshToken(refreshToken));
    }

    @Test
    public void testValidateRefreshTokenWrongType() {
        // Passing an access token where a refresh token is expected.
        Mockito.when(tokenRepository.findByToken(token))
                .thenReturn(Optional.of(Token.builder().token(token).active(true).build()));

        Assertions.assertThrows(JwtException.class, () -> jwtService.validateRefreshToken(token));
    }

    @Test
    public void testDeactivateRefreshTokenSuccess() {
        String refreshToken = jwtService.createRefreshToken(user, Role.USER);
        Token tokenEntity = Token.builder().token(refreshToken).active(true).build();
        Mockito.when(tokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(tokenEntity));
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenAnswer(i -> i.getArgument(0));

        jwtService.deactivateRefreshToken(refreshToken);

        Assertions.assertFalse(tokenEntity.isActive());
        Mockito.verify(tokenRepository).save(tokenEntity);
    }

    @Test
    public void testDeactivateRefreshTokenNotFound() {
        String refreshToken = jwtService.createRefreshToken(user, Role.USER);
        Mockito.when(tokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        Assertions.assertThrows(JwtException.class, () -> jwtService.deactivateRefreshToken(refreshToken));
        Mockito.verify(tokenRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testDeactivateRefreshTokenAlreadyInactive() {
        String refreshToken = jwtService.createRefreshToken(user, Role.USER);
        Token tokenEntity = Token.builder().token(refreshToken).active(false).build();
        Mockito.when(tokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(tokenEntity));

        Assertions.assertThrows(JwtException.class, () -> jwtService.deactivateRefreshToken(refreshToken));
        Mockito.verify(tokenRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testDeactivateAccessTokenThrows() {
        // Access tokens should not be deactivatable.
        Token tokenEntity = Token.builder().token(token).active(true).build();
        Mockito.when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenEntity));

        Assertions.assertThrows(JwtException.class, () -> jwtService.deactivateRefreshToken(token));
        Mockito.verify(tokenRepository, Mockito.never()).save(Mockito.any());
    }
}
