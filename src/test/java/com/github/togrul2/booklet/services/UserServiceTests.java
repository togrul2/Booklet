package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.user.CreateUserDto;
import com.github.togrul2.booklet.dtos.user.UpdateUserDto;
import com.github.togrul2.booklet.dtos.user.UserDto;
import com.github.togrul2.booklet.dtos.user.UserFilterDto;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
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
        Mockito.when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encoded");
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);

        UserDto userDto = userService.register(createUserDto);

        Assertions.assertNotNull(userDto);
        Assertions.assertEquals(user.getEmail(), userDto.email());
        Mockito.verify(userRepository).findByEmail(Mockito.anyString());
        Mockito.verify(userRepository).save(Mockito.any());
        Mockito.verify(passwordEncoder).encode(Mockito.anyString());
    }

    @Test
    public void testRegisterWithExistingEmail() {
        Mockito.when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(anotherUser));

        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.register(createUserDto));
        Mockito.verify(userRepository).findByEmail(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));

        UserFilterDto filter = UserFilterDto.builder().email("johndoe").build();
        Page<UserDto> users = userService.findAll(pageable, filter);

        Assertions.assertNotNull(users);
        Assertions.assertEquals(1, users.getTotalElements());
        Mockito.verify(userRepository).findAll(pageable);
    }

    @Test
    public void testFindAllEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<UserDto> users = userService.findAll(pageable, UserFilterDto.builder().build());

        Assertions.assertTrue(users.isEmpty());
    }

    @Test
    public void testFindById() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto userDto = userService.findById(1L);

        Assertions.assertNotNull(userDto);
        Assertions.assertEquals(user.getEmail(), userDto.email());
        Mockito.verify(userRepository).findById(1L);
    }

    @Test
    public void testFindByIdNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.findById(1L));
        Mockito.verify(userRepository).findById(1L);
    }

    @Test
    public void testUpdateSuccess() {
        UpdateUserDto dto = new UpdateUserDto("newemail@example.com", "Jane", "Smith");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));

        UserDto result = userService.update(1L, dto);

        Assertions.assertEquals("newemail@example.com", result.email());
        Assertions.assertEquals("Jane", result.firstName());
        Mockito.verify(userRepository).save(Mockito.any());
    }

    @Test
    public void testUpdateNotFound() {
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> userService.update(99L, new UpdateUserDto(null, "Jane", "Smith")));
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testUpdateEmailAlreadyTaken() {
        UpdateUserDto dto = new UpdateUserDto(anotherUser.getEmail(), "Jane", "Smith");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // Email belongs to a different user
        Mockito.when(userRepository.findByEmail(anotherUser.getEmail())).thenReturn(Optional.of(anotherUser));

        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.update(1L, dto));
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testUpdateSameEmailAllowed() {
        // Updating with the same email as the current user should be allowed
        UpdateUserDto dto = new UpdateUserDto(user.getEmail(), "Johnny", "Doe");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));

        Assertions.assertDoesNotThrow(() -> userService.update(1L, dto));
        Mockito.verify(userRepository).save(Mockito.any());
    }

    @Test
    public void testDeleteSuccess() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Assertions.assertDoesNotThrow(() -> userService.delete(1L));
        Mockito.verify(userRepository).delete(user);
    }

    @Test
    public void testDeleteNotFound() {
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.delete(99L));
        Mockito.verify(userRepository, Mockito.never()).delete(Mockito.any(User.class));
    }

    @Test
    public void testFindAuthUserSuccess() {
        Mockito.when(userRepository.findAuthUser()).thenReturn(Optional.of(user));

        UserDto result = userService.findAuthUser();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getEmail(), result.email());
        Mockito.verify(userRepository).findAuthUser();
    }

    @Test
    public void testFindAuthUserNotFound() {
        Mockito.when(userRepository.findAuthUser()).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.findAuthUser());
    }

    @Test
    public void testUpdateAuthUserSuccess() {
        UpdateUserDto dto = new UpdateUserDto("new@example.com", "Johnny", "Doe");
        Mockito.when(userRepository.findAuthUser()).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));

        UserDto result = userService.updateAuthUser(dto);

        Assertions.assertEquals("new@example.com", result.email());
        Mockito.verify(userRepository).save(Mockito.any());
    }

    @Test
    public void testUpdateAuthUserNotFound() {
        Mockito.when(userRepository.findAuthUser()).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> userService.updateAuthUser(new UpdateUserDto(null, "Johnny", "Doe")));
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testDeleteAuthUserSuccess() {
        Mockito.when(userRepository.findAuthUser()).thenReturn(Optional.of(user));

        Assertions.assertDoesNotThrow(() -> userService.deleteAuthUser());
        Mockito.verify(userRepository).delete(user);
    }

    @Test
    public void testDeleteAuthUserNotFound() {
        Mockito.when(userRepository.findAuthUser()).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.deleteAuthUser());
        Mockito.verify(userRepository, Mockito.never()).delete(Mockito.any(User.class));
    }
}