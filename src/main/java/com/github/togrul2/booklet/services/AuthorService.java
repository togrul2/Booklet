package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.author.AuthorDto;
import com.github.togrul2.booklet.dtos.author.CreateAuthorDto;
import com.github.togrul2.booklet.dtos.author.UpdateAuthorDto;
import com.github.togrul2.booklet.entities.Author;
import com.github.togrul2.booklet.exceptions.AuthorNotFound;
import com.github.togrul2.booklet.mappers.AuthorMapper;
import com.github.togrul2.booklet.repositories.AuthorRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;

    public Page<AuthorDto> findAll(Pageable pageable) {
        return authorRepository
                .findAll(pageable)
                .map(AuthorMapper.INSTANCE::toAuthorDto);
    }

    public AuthorDto findOneById(long id) {
        return authorRepository
                .findById(id)
                .map(AuthorMapper.INSTANCE::toAuthorDto)
                .orElseThrow(AuthorNotFound::new);
    }

    public AuthorDto create(CreateAuthorDto createAuthorDto) {
        Author author = AuthorMapper.INSTANCE.toAuthor(createAuthorDto);
        return AuthorMapper.INSTANCE.toAuthorDto(authorRepository.save(author));
    }

    public AuthorDto replace(long id, CreateAuthorDto createAuthorDto) {
        if (!authorRepository.existsById(id)) {
            throw new AuthorNotFound();
        }

        Author author = AuthorMapper.INSTANCE.toAuthor(createAuthorDto);
        author.setId(id);
        return AuthorMapper.INSTANCE.toAuthorDto(authorRepository.save(author));
    }

    public AuthorDto update(long id, @NonNull UpdateAuthorDto updateAuthorDto) {
        Author author = authorRepository
                .findById(id)
                .orElseThrow(AuthorNotFound::new);

        if (updateAuthorDto.name() != null) {
            author.setName(updateAuthorDto.name());
        }
        if (updateAuthorDto.surname() != null) {
            author.setSurname(updateAuthorDto.surname());
        }
        if (updateAuthorDto.birthDate() != null) {
            author.setBirthDate(updateAuthorDto.birthDate());
        }
        if (updateAuthorDto.deathDate() != null) {
            author.setDeathDate(updateAuthorDto.deathDate());
        }
        if (updateAuthorDto.biography() != null) {
            author.setBiography(updateAuthorDto.biography());
        }

        return AuthorMapper.INSTANCE.toAuthorDto(authorRepository.save(author));
    }

    public void delete(long id) {
        authorRepository.deleteById(id);
    }
}
