package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.user.CreateUserDto;
import com.github.togrul2.booklet.dtos.user.PartialUpdateUserDto;
import com.github.togrul2.booklet.dtos.user.UpdateUserDto;
import com.github.togrul2.booklet.dtos.user.UserDto;
import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.exceptions.UserNotFound;
import com.github.togrul2.booklet.mappers.UserMapper;
import com.github.togrul2.booklet.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private void checkEmailAvailability(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already taken");
        }
    }

    /**
     * Checks email availability for user with given id.
     *
     * @param email  email to check.
     * @param userId userId to exclude from search.
     * @throws IllegalArgumentException If email with given id belongs to user with different id.
     */
    private void checkEmailAvailability(String email, long userId) {
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new IllegalArgumentException("Email already taken");
        }
    }

    public UserDto register(@NonNull CreateUserDto createUserDto) {
        checkEmailAvailability(createUserDto.email());
        User user = UserMapper.INSTANCE.toUser(createUserDto);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(createUserDto.password()));
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper.INSTANCE::toUserDto);
    }

    public UserDto findById(long id) {
        return userRepository
                .findById(id)
                .map(UserMapper.INSTANCE::toUserDto)
                .orElseThrow(UserNotFound::new);
    }

    public UserDto findByEmail(String username) {
        return userRepository
                .findByEmail(username)
                .map(UserMapper.INSTANCE::toUserDto)
                .orElseThrow(UserNotFound::new);
    }

    private UserDto replaceUser(@NonNull User user, @NonNull UpdateUserDto updateUserDto) {
        user.setEmail(updateUserDto.email());
        user.setFirstName(updateUserDto.firstName());
        user.setLastName(updateUserDto.lastName());
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    public UserDto replace(long userId, @NonNull UpdateUserDto updateUserDto) {
        checkEmailAvailability(updateUserDto.email(), userId);
        User user = userRepository.findById(userId).orElseThrow(UserNotFound::new);
        return replaceUser(user, updateUserDto);
    }

    public UserDto replace(String email, @NonNull UpdateUserDto updateUserDto) {
        checkEmailAvailability(updateUserDto.email());
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFound::new);
        return replaceUser(user, updateUserDto);
    }

    private UserDto updateUser(User user, PartialUpdateUserDto partialUpdateUserDto) {
        if (partialUpdateUserDto.email() != null) {
            user.setEmail(partialUpdateUserDto.email());
        }

        if (partialUpdateUserDto.firstName() != null) {
            user.setFirstName(partialUpdateUserDto.firstName());
        }

        if (partialUpdateUserDto.lastName() != null) {
            user.setLastName(partialUpdateUserDto.lastName());
        }

        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    public UserDto update(long userId, @NonNull PartialUpdateUserDto partialUpdateUserDto) {
        Optional
                .ofNullable(partialUpdateUserDto.email())
                .ifPresent(email -> checkEmailAvailability(email, userId));

        User user = userRepository.findById(userId).orElseThrow(UserNotFound::new);
        return updateUser(user, partialUpdateUserDto);
    }

    public UserDto update(String email, @NonNull PartialUpdateUserDto partialUpdateUserDto) {
        Optional
                .ofNullable(partialUpdateUserDto.email())
                .ifPresent(this::checkEmailAvailability);

        User user = userRepository.findByEmail(email).orElseThrow(UserNotFound::new);
        return updateUser(user, partialUpdateUserDto);
    }

    public void delete(long userId) {
        userRepository.deleteById(userId);
    }

    public void delete(String email) {
        userRepository.deleteByEmail(email);
    }
}
