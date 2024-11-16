package com.github.togrul2.booklet.repositories;

import com.github.togrul2.booklet.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Modifying
    @Query("DELETE FROM User WHERE email = :email")
    void deleteByEmail(String email);
}
