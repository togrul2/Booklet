package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.entities.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
public class JwtServiceTests {
    @InjectMocks
    private JwtService jwtService;
    private final String username = "someusername";
    private UserDetails user;
    private String token;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(
                jwtService, "secretKey", "bxBWG9rIQcquX1UubhgI5lIqF4B6+N0GBBN5v/eCSss="
        );
        user = User.builder()
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
    public void testIsAccessToken() {
        Assertions.assertTrue(jwtService.isAccessToken(token));
    }

    @Test
    public void testIsRefreshToken() {
        String token = jwtService.createRefreshToken(user, Role.USER);
        Assertions.assertTrue(jwtService.isRefreshToken(token));
    }

    @Test
    public void testCreateAccessToken() {
        String accessToken = jwtService.createAccessToken(user, Role.USER);
        Assertions.assertNotNull(accessToken);
        Assertions.assertTrue(jwtService.isAccessToken(accessToken));
        // Test expiration.
        Assertions.assertEquals(username, jwtService.extractUsername(accessToken));
    }

    @Test
    public void testCreateRefreshToken() {
        String refreshToken = jwtService.createRefreshToken(user, Role.USER);
        Assertions.assertNotNull(refreshToken);
        Assertions.assertTrue(jwtService.isRefreshToken(refreshToken));
        // Test expiration.
        Assertions.assertEquals(username, jwtService.extractUsername(refreshToken));
    }
}
