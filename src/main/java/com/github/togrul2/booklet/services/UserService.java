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
import com.github.togrul2.booklet.security.annotations.IsAdmin;
import com.github.togrul2.booklet.security.annotations.IsAuthenticated;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * Registers new user. Authenticated user must be anonymous, thus not authenticated.
     * @param createUserDto request dto.
     * @return created user.
     */
    @PreAuthorize("isAnonymous()")
    public UserDto register(@NonNull CreateUserDto createUserDto) {
        checkEmailAvailability(createUserDto.email());
        User user = UserMapper.INSTANCE.toUser(createUserDto);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(createUserDto.password()));
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    @IsAdmin
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper.INSTANCE::toUserDto);
    }

    /**
     * Finds authenticated user.
     * @throws UserNotFound If authenticated user does not exist.
     * @return authenticated user.
     */
    @IsAuthenticated
    public UserDto findAuthUser() {
        return userRepository
                .findAuthUser()
                .map(UserMapper.INSTANCE::toUserDto)
                .orElseThrow(UserNotFound::new);
    }

    @IsAuthenticated
    public UserDto replaceAuthUser(@NonNull UpdateUserDto updateUserDto) {
        return replace(findAuthUser().id(), updateUserDto);
    }

    @IsAuthenticated
    public UserDto updateAuthUser(@NonNull PartialUpdateUserDto partialUpdateUserDto) {
        return update(findAuthUser().id(), partialUpdateUserDto);
    }

    @IsAuthenticated
    public void deleteAuthUser() {
        delete(findAuthUser().id());
    }

    public UserDto findById(long id) {
        return UserMapper.INSTANCE.toUserDto(findUserById(id));
    }

    /**
     * Finds user by id. Authenticated user must be an admin or target user.
     * @param id    id of user to find.
     * @throws UserNotFound If user with given id does not exist.
     * @return user with given id.
     */
    @PostAuthorize("hasRole('ADMIN') or returnObject.email == principal.username")
    public User findUserById(long id) {
        return userRepository.findById(id).orElseThrow(UserNotFound::new);
    }

    /**
     * Replaces user with given id. Authenticated user must be an admin or target user.
     * @param id id of user to update.
     * @param updateUserDto request dto.
     * @throws IllegalArgumentException If email is already taken.
     * @throws UserNotFound If user with given id does not exist.
     * @return updated user.
     */
    public UserDto replace(long id, @NonNull UpdateUserDto updateUserDto) {
        User user = findUserById(id);
        checkEmailAvailability(updateUserDto.email(), id);
        user.setEmail(updateUserDto.email());
        user.setFirstName(updateUserDto.firstName());
        user.setLastName(updateUserDto.lastName());
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    /**
     * Updates user with given id. Authenticated user must be an admin or target user.
     * @param id id of user to update.
     * @param partialUpdateUserDto request dto.
     * @throws IllegalArgumentException If email is already taken.
     * @throws UserNotFound If user with given id does not exist.
     * @return updated user.
     */
    public UserDto update(long id, @NonNull PartialUpdateUserDto partialUpdateUserDto) {
        User user = findUserById(id);

        // See if email is available if it gets updated.
        Optional
                .ofNullable(partialUpdateUserDto.email())
                .ifPresent(email -> checkEmailAvailability(email, id));

        // Update user fields if they are not null.
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

    /**
     * Deletes user with given id. Authenticated user must be an admin or target user.
     * @param id id of user to delete.
     * @throws UserNotFound If user with given id does not exist.
     */
    public void delete(long id) {
        UserDto user = findById(id);
        userRepository.deleteById(user.id());
    }
}
