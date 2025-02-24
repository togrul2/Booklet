package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.user.*;
import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.mappers.UserMapper;
import com.github.togrul2.booklet.repositories.UserRepository;
import com.github.togrul2.booklet.annotations.IsAdmin;
import com.github.togrul2.booklet.annotations.IsAuthenticated;
import com.github.togrul2.booklet.annotations.IsUser;
import com.github.togrul2.booklet.specifications.UserSpecificationAssembler;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Validates user. Throws exception if email is already taken.
     *
     * @param user user to validate.
     * @throws IllegalArgumentException If email is already taken by another user.
     */
    private void validateUser(User user) {
        userRepository.findByEmail(user.getEmail()).ifPresent(u -> {
            if (user.getId() == null || !Objects.equals(u.getId(), user.getId())) {
                throw new IllegalArgumentException("Email already taken.");
            }
        });
    }

    /**
     * Registers new user. Authenticated user must be anonymous.
     *
     * @param createUserDto request dto.
     * @return created user.
     * @throws IllegalArgumentException If email is already taken.
     */
    @PreAuthorize("isAnonymous()")
    public UserDto register(CreateUserDto createUserDto) {
        User user = UserMapper.INSTANCE.toUser(createUserDto);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(createUserDto.password()));
        validateUser(user);
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    @IsAdmin
    public Page<UserDto> findAll(Pageable pageable, UserFilterDto filterDto) {
        Optional<Specification<User>> specification = UserSpecificationAssembler
                .builder()
                .filterDto(filterDto)
                .build()
                .getSpecification();
        return specification
                .map(s -> userRepository.findAll(s, pageable))
                .orElseGet(() -> userRepository.findAll(pageable))
                .map(UserMapper.INSTANCE::toUserDto);
    }

    @IsUser
    public UserDto findById(long id) {
        return userRepository
                .findById(id)
                .map(UserMapper.INSTANCE::toUserDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private UserDto replaceUser(User user, UpdateUserDto updateUserDto) {
        user.setEmail(updateUserDto.email());
        user.setFirstName(updateUserDto.firstName());
        user.setLastName(updateUserDto.lastName());
        validateUser(user);
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    private UserDto updateUser(User user, PartialUpdateUserDto partialUpdateUserDto) {
        Optional.ofNullable(partialUpdateUserDto.email()).ifPresent(user::setEmail);
        Optional.ofNullable(partialUpdateUserDto.firstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(partialUpdateUserDto.lastName()).ifPresent(user::setLastName);
        validateUser(user);
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    /**
     * Replaces user with given id. Authenticated user must be an admin or target user.
     *
     * @param id            id of user to update.
     * @param updateUserDto request dto.
     * @return updated user.
     * @throws IllegalArgumentException If email is already taken.
     * @throws ResourceNotFoundException()  If user with given id does not exist.
     */
    @IsUser
    public UserDto replace(long id, UpdateUserDto updateUserDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return replaceUser(user, updateUserDto);
    }

    /**
     * Updates user with given id. Authenticated user must be an admin or target user.
     *
     * @param id                   id of user to update.
     * @param partialUpdateUserDto request dto.
     * @return updated user.
     * @throws IllegalArgumentException If email is already taken.
     * @throws ResourceNotFoundException()  If user with given id does not exist.
     */
    @IsUser
    public UserDto update(long id, PartialUpdateUserDto partialUpdateUserDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return updateUser(user, partialUpdateUserDto);
    }

    /**
     * Deletes user with given id. Authenticated user must be an admin or target user.
     *
     * @param id id of user to delete.
     * @throws ResourceNotFoundException() If user with given id does not exist.
     */
    @IsUser
    public void delete(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        userRepository.delete(user);
    }

    /**
     * Finds authenticated user.
     *
     * @return authenticated user.
     * @throws ResourceNotFoundException() If authenticated user does not exist.
     */
    @IsAuthenticated
    public UserDto findAuthUser() {
        return userRepository
                .findAuthUser()
                .map(UserMapper.INSTANCE::toUserDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    @IsAuthenticated
    public UserDto replaceAuthUser(UpdateUserDto updateUserDto) {
        User user = userRepository.findAuthUser().orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return replaceUser(user, updateUserDto);
    }

    @IsAuthenticated
    public UserDto updateAuthUser(PartialUpdateUserDto partialUpdateUserDto) {
        User user = userRepository.findAuthUser().orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return updateUser(user, partialUpdateUserDto);
    }

    @IsAuthenticated
    public void deleteAuthUser() {
        User user = userRepository.findAuthUser().orElseThrow(() -> new ResourceNotFoundException("User not found."));
        userRepository.delete(user);
    }
}
