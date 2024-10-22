package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.user.CreateUserDto;
import com.github.togrul2.booklet.dtos.user.UserDto;
import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.entities.User;
import com.github.togrul2.booklet.exceptions.TakenAttributeException;
import com.github.togrul2.booklet.exceptions.UserNotFound;
import com.github.togrul2.booklet.mappers.UserMapper;
import com.github.togrul2.booklet.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private void validateUniqueFields(CreateUserDto createUserDto) {
        if (userRepository.existsByEmail(createUserDto.email()))
            throw new TakenAttributeException("Email already taken");
    }

    public UserDto register(CreateUserDto createUserDto) {
        validateUniqueFields(createUserDto);
        User user = UserMapper.INSTANCE.toUser(createUserDto);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(createUserDto.password()));
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    public Page<UserDto> findAll(int pageNumber, int pageSize) {
        return userRepository
                .findAll(PageRequest.of(pageNumber - 1, pageSize))
                .map(UserMapper.INSTANCE::toUserDto);
    }

    public UserDto findById(long id) {
        return userRepository.findById(id)
                .map(UserMapper.INSTANCE::toUserDto)
                .orElseThrow(UserNotFound::new);
    }
}
