package com.github.togrul2.booklet.repositories;

import com.github.togrul2.booklet.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = ?#{ principal?.username }")
    Optional<User> findAuthUser();

    /** Finds user by id. Authenticated user must be the target user or admin.
     * @param id target user's id.
     * @return Optional of user entity.
     */
    @Query(
            "SELECT u FROm User u " +
            "WHERE u.id = ?1 AND u.email = ?#{ principal?.username } " +
            "OR ?#{principal?.authorities.contains('ROLE_ADMIN')} = true"
    )
    Optional<User> findById(long id);
}
