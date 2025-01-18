package com.github.togrul2.booklet.repositories;

import com.github.togrul2.booklet.entities.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByName(String name);

    Optional<Genre> findBySlug(String slug);
}
