package com.github.togrul2.booklet.repositories;

import com.github.togrul2.booklet.annotations.IsAdmin;
import com.github.togrul2.booklet.entities.Genre;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;


@Tag(name = "Genres")
@RepositoryRestResource(collectionResourceRel = "genres", path = "genres")
public interface GenreRepository extends CrudRepository<Genre, Long> {
    @Cacheable(cacheNames = "genre", key = "'slug:' + #slug")
    Genre findBySlug(String slug);

    @Cacheable(cacheNames = "genre", key = "'id:' + #id")
    Optional<Genre> findById(long id);

    @Override
    @Cacheable(cacheNames = "genres")
    Iterable<Genre> findAll();

    @IsAdmin
    @Caching(
            evict = @CacheEvict(cacheNames = "genres", allEntries = true),
            put = {
                    @CachePut(cacheNames = "genre", key = "'name:' + #entity.name"),
                    @CachePut(cacheNames = "genre", key = "'slug:' + #entity.slug"),
                    @CachePut(cacheNames = "genre", key = "'id' + #entity.id")
            }
    )
    <S extends Genre> S save(S entity);

    @IsAdmin
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "genres", allEntries = true),
                    @CacheEvict(cacheNames = "genre", key = "'name:' + #entity.name"),
                    @CacheEvict(cacheNames = "genre", key = "'slug:' + #entity.slug"),
                    @CacheEvict(cacheNames = "genre", key = "'id' + #entity.id")
            }
    )
    void delete(Genre entity);
}
