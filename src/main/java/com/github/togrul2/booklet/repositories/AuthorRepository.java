package com.github.togrul2.booklet.repositories;

import com.github.togrul2.booklet.entities.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuthorRepository extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {
}
