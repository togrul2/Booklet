package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.user.CreateUserDto;
import com.github.togrul2.booklet.dtos.user.UserDto;
import com.github.togrul2.booklet.dtos.user.UserFilterDto;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class UserServiceTests {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private User user, anotherUser;
    private CreateUserDto createUserDto;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(1L)
                .email("johndoe@example.com")
                .password("encoded")
                .firstName("John")
                .lastName("Doe")
                .build();
        anotherUser = User.builder()
                .id(2L)
                .email("anotherjohndoe@example.com")
                .password("encoded")
                .firstName("Another John")
                .lastName("Doe")
                .build();

        createUserDto = CreateUserDto.builder()
                .email("johndoe@example.com")
                .password("secret")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    public void testRegister() {
        // Arrange
        Mockito
                .when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Optional.empty());
        Mockito
                .when(passwordEncoder.encode(Mockito.anyString()))
                .thenReturn("encoded");
        Mockito
                .when(userRepository.save(Mockito.any()))
                .thenReturn(user);

        // Act
        UserDto userDto = userService.register(createUserDto);
        // Assert
        Assertions.assertNotNull(userDto);
        Assertions.assertEquals(user.getEmail(), userDto.email());
        Mockito
                .verify(userRepository, Mockito.times(1))
                .findByEmail(Mockito.anyString());
        Mockito
                .verify(userRepository, Mockito.times(1))
                .save(Mockito.any());
        Mockito
                .verify(passwordEncoder, Mockito.times(1))
                .encode(Mockito.anyString());
    }

    @Test
    public void testRegisterWithExistingEmail() {
        Mockito
                .when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Optional.of(anotherUser));

        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.register(createUserDto));
        Mockito
                .verify(userRepository, Mockito.times(1))
                .findByEmail(Mockito.anyString());
    }

    @Test
    public void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Mockito
                .when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(user)));

        UserFilterDto filter = UserFilterDto.builder().email("johndoe").build();
        Page<UserDto> users = userService.findAll(pageable, filter);
        Assertions.assertNotNull(users);
        Assertions.assertEquals(1, users.getTotalElements());
        Mockito
                .verify(userRepository, Mockito.times(1))
                .findAll(pageable);
    }

    @Test
    public void testFindById() {
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(java.util.Optional.of(user));

        UserDto userDto = userService.findById(1L);
        Assertions.assertNotNull(userDto);
        Assertions.assertEquals(user.getEmail(), userDto.email());
        Mockito
                .verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
    }

    @Test
    @Disabled
    public void testFindByIdWithNonExistingUser() {
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(java.util.Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.findById(1L));
        Mockito
                .verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());

    }
}
