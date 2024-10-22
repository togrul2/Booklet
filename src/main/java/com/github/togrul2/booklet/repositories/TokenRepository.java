package com.github.togrul2.booklet.repositories;

import com.github.togrul2.booklet.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    @Query("SELECT t.active FROM Token t WHERE t.token = :token")
    boolean isTokenActive(String token);
}
