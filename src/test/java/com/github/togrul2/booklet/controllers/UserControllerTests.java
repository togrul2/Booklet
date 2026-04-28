package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.configurations.TestcontainersConfiguration;
import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.entities.User;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

@WithMockUser
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        User authUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@example.com")
                .password("Password123$")
                .role(Role.ADMIN)
                .build();
        userRepository.save(authUser);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.authentication = RestAssured.oauth2(jwtService.createAccessToken(authUser, authUser.getRole()));
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void testGetAuthUser() {
        RestAssured.given()
                .when()
                .get("/api/v1/users/me")
                .then()
                .statusCode(200)
                .body("email", Matchers.is("johndoe@example.com"));
    }
}
