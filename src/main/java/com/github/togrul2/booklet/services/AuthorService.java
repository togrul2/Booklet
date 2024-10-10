package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.AuthorDto;
import com.github.togrul2.booklet.dtos.CreateAuthorDto;
import com.github.togrul2.booklet.dtos.UpdateAuthorDto;
import com.github.togrul2.booklet.entities.Author;
import com.github.togrul2.booklet.exceptions.AuthorNotFound;
import com.github.togrul2.booklet.mappers.AuthorMapper;
import com.github.togrul2.booklet.repositories.AuthorRepository;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;

    public Page<AuthorDto> findAll(int pageNumber, int pageSize) {
        return authorRepository.findAll(PageRequest.of(pageNumber - 1, pageSize)).map(AuthorMapper.INSTANCE::toAuthorDto);
    }

    public AuthorDto findOneById(long id) {
        return AuthorMapper.INSTANCE.toAuthorDto(authorRepository.findById(id).orElseThrow(AuthorNotFound::new));
    }

    public AuthorDto create(CreateAuthorDto createAuthorDto) {
        return AuthorMapper.INSTANCE.toAuthorDto(
                authorRepository.save(AuthorMapper.INSTANCE.toAuthor(createAuthorDto))
        );
    }

    public AuthorDto replace(long id, CreateAuthorDto createAuthorDto) {
        if (!authorRepository.existsById(id))
            throw new AuthorNotFound();

        Author author = AuthorMapper.INSTANCE.toAuthor(createAuthorDto);
        author.setId(id);
        return AuthorMapper.INSTANCE.toAuthorDto(authorRepository.save(author));
    }

    public AuthorDto update(long id, UpdateAuthorDto updateAuthorDto) {
        Author author = authorRepository.findById(id).orElseThrow(AuthorNotFound::new);

        if (updateAuthorDto.name() != null)
            author.setName(updateAuthorDto.name());

        if (updateAuthorDto.surname() != null)
            author.setSurname(updateAuthorDto.surname());

        if (updateAuthorDto.birthDate() != null)
            author.setBirthDate(updateAuthorDto.birthDate());

        if (updateAuthorDto.deathDate() != null)
            author.setDeathDate(updateAuthorDto.deathDate());

        if (updateAuthorDto.biography() != null)
            author.setBiography(updateAuthorDto.biography());

        return AuthorMapper.INSTANCE.toAuthorDto(authorRepository.save(author));
    }

    public void delete(long id) {
        authorRepository.deleteById(id);
    }
}
