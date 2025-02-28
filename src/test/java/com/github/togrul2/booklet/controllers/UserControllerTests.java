package com.github.togrul2.booklet.controllers;

import com.github.togrul2.booklet.dtos.user.UserDto;
import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@WithMockUser
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @LocalServerPort
    private int port;
    private String domain;

    @BeforeEach
    public void setUp() {
        userRepository.save(
            User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("admin@example.com")
                .password("Password123$")
                .role(Role.ADMIN)
                .build()
        );
        domain = "http://localhost:" + port;
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void testGetAuthUser() {
        ResponseEntity<UserDto> response = restTemplate.getForEntity(
            domain + "/api/v1/users/me", UserDto.class
        );
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
    }
}
