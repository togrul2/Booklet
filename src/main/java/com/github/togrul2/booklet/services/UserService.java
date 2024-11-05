package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.user.CreateUserDto;
import com.github.togrul2.booklet.dtos.user.PartialUpdateUserDto;
import com.github.togrul2.booklet.dtos.user.UpdateUserDto;
import com.github.togrul2.booklet.dtos.user.UserDto;
import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.exceptions.TakenAttributeException;
import com.github.togrul2.booklet.exceptions.UserNotFound;
import com.github.togrul2.booklet.mappers.UserMapper;
import com.github.togrul2.booklet.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private void validateUniqueFields(@NonNull CreateUserDto createUserDto) {
        if (userRepository.existsByEmail(createUserDto.email())) {
            throw new TakenAttributeException("Email already taken");
        }
    }

    private void validateUniqueFields(@NonNull UpdateUserDto createUserDto, Long userId) {
        if (userRepository.existsByEmailAndIdNot(createUserDto.email(), userId)) {
            throw new TakenAttributeException("Email already taken");
        }
    }

    private void validateUniqueFields(@NonNull PartialUpdateUserDto partialUpdateUserDto, Long userId) {
        if (
                partialUpdateUserDto.email() != null &&
                        userRepository.existsByEmailAndIdNot(partialUpdateUserDto.email(), userId)
        ) {
            throw new TakenAttributeException("Email already taken");
        }
    }

    public UserDto register(CreateUserDto createUserDto) {
        validateUniqueFields(createUserDto);
        User user = UserMapper.INSTANCE.toUser(createUserDto);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(createUserDto.password()));
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository
                .findAll(pageable)
                .map(UserMapper.INSTANCE::toUserDto);
    }

    public UserDto findById(long id) {
        return userRepository.findById(id)
                .map(UserMapper.INSTANCE::toUserDto)
                .orElseThrow(UserNotFound::new);
    }

    public UserDto replace(long userId, UpdateUserDto updateUserDto) {
        validateUniqueFields(updateUserDto, userId);
        User user = userRepository.findById(userId).orElseThrow(UserNotFound::new);
        user.setEmail(updateUserDto.email());
        user.setFirstName(updateUserDto.firstName());
        user.setLastName(updateUserDto.lastName());
        user = userRepository.save(user);
        return UserMapper.INSTANCE.toUserDto(user);
    }

    public UserDto update(long userId, PartialUpdateUserDto partialUpdateUserDto) {
        validateUniqueFields(partialUpdateUserDto, userId);
        User user = userRepository.findById(userId).orElseThrow(UserNotFound::new);
        if (partialUpdateUserDto.email() != null) {
            user.setEmail(partialUpdateUserDto.email());
        }
        if (partialUpdateUserDto.firstName() != null) {
            user.setFirstName(partialUpdateUserDto.firstName());
        }
        if (partialUpdateUserDto.lastName() != null) {
            user.setLastName(partialUpdateUserDto.lastName());
        }
        user = userRepository.save(user);
        return UserMapper.INSTANCE.toUserDto(user);
    }

    public void delete(long userId) {
        userRepository.deleteById(userId);
    }
}
