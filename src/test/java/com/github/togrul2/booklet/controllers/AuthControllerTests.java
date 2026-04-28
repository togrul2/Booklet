package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.configurations.TestcontainersConfiguration;
import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.repositories.TokenRepository;
import com.github.togrul2.booklet.repositories.UserRepository;
import com.github.togrul2.booklet.services.JwtService;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@Import(TestcontainersConfiguration.class)
public class AuthControllerTests {
    private final String userPassword = "Password123$";
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TokenRepository tokenRepository;
    @LocalServerPort
    private int port;
    private User user;
    private String refreshToken;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

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
        RestAssured.given()
                .contentType("application/json")
                .body("""
                        {
                           "email": "%s",
                           "password": "%s"
                        }
                        """.formatted(user.getEmail(), userPassword))
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", Matchers.is(Matchers.notNullValue()))
                .body("refreshToken", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testRefresh() {
        RestAssured.given()
                .contentType("application/json")
                .body("{\"refreshToken\": \"%s\"}".formatted(refreshToken))
                .when()
                .post("/api/v1/auth/refresh")
                .then()
                .statusCode(200)
                .body("accessToken", Matchers.is(Matchers.notNullValue()))
                .body("refreshToken", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testLogout() {
        RestAssured.given()
                .contentType("application/json")
                .body("{\"refreshToken\": \"%s\"}".formatted(refreshToken))
                .when()
                .post("/api/v1/auth/logout")
                .then()
                .statusCode(200);
    }

    @Test
    public void testValidate() {
        RestAssured.given()
                .contentType("application/json")
                .body("{\"refreshToken\": \"%s\"}".formatted(refreshToken))
                .when()
                .post("/api/v1/auth/validate")
                .then()
                .statusCode(200);
    }
}
