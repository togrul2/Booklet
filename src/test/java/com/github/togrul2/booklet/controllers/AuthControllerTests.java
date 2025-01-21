package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.auth.LoginDto;
import com.github.togrul2.booklet.dtos.auth.RefreshRequestDto;
import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.repositories.TokenRepository;
import com.github.togrul2.booklet.repositories.UserRepository;
import com.github.togrul2.booklet.services.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class AuthControllerTests {
    private final String userPassword = "Password123$";
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @LocalServerPort
    private int port;
    private String domain;
    private User user;
    private String refreshToken;
    @Autowired
    private TokenRepository tokenRepository;

    @BeforeEach
    public void setUp() {
        domain = "http://localhost:" + port;
        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .role(Role.USER)
                .active(true)
                .email("test@example.com")
                .password(passwordEncoder.encode(userPassword))
                .build();
        userRepository.save(user);
        refreshToken = jwtService.createAndStoreRefreshToken(user, Role.USER).getToken();
    }

    @AfterEach
    public void tearDown() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testLogin() {
        LoginDto requestBody = LoginDto
                .builder()
                .email(user.getEmail())
                .password(userPassword)
                .build();
        ResponseEntity<TokenPairDto> response = restTemplate.postForEntity(
                domain + "/api/v1/auth/login", requestBody, TokenPairDto.class
        );
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testRefresh() {
        // Create a refresh token for the user and save as active.
        RefreshRequestDto requestBody = new RefreshRequestDto(refreshToken);
        ResponseEntity<TokenPairDto> response = restTemplate.postForEntity(
                domain + "/api/v1/auth/refresh", requestBody, TokenPairDto.class
        );
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testLogout() {
        RefreshRequestDto requestBody = new RefreshRequestDto(refreshToken);
        ResponseEntity<Void> response = restTemplate.postForEntity(
                domain + "/api/v1/auth/logout", requestBody, Void.class
        );
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNull(response.getBody());

    }

    @Test
    public void testValidate() {
        RefreshRequestDto requestBody = new RefreshRequestDto(refreshToken);
        ResponseEntity<Void> response = restTemplate.postForEntity(
                domain + "/api/v1/auth/validate", requestBody, Void.class
        );
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNull(response.getBody());
    }
}
