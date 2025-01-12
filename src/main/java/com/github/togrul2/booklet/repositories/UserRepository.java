package com.github.togrul2.booklet.repositories;

import com.github.togrul2.booklet.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = #{principal.username}")
    Optional<User> findAuthUser();

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
