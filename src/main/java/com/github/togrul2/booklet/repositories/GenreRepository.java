package com.github.togrul2.booklet.repositories;

import com.github.togrul2.booklet.entities.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, long id);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, long id);
}
