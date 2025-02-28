package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.annotations.IsAdmin;
import com.github.togrul2.booklet.dtos.author.AuthorDto;
import com.github.togrul2.booklet.dtos.author.AuthorFilterDto;
import com.github.togrul2.booklet.dtos.author.AuthorRequestDto;
import com.github.togrul2.booklet.entities.Author;
import com.github.togrul2.booklet.mappers.AuthorMapper;
import com.github.togrul2.booklet.repositories.AuthorRepository;
import com.github.togrul2.booklet.specifications.AuthorSpecificationAssembler;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;

    public Page<AuthorDto> findAll(Pageable pageable, AuthorFilterDto filterDto) {
        Optional<Specification<Author>> specification = AuthorSpecificationAssembler
                .builder()
                .filterDto(filterDto)
                .build()
                .getSpecification();

        // If specification is present, use it. Otherwise, get all authors.
        return specification
                .map(s -> authorRepository.findAll(s, pageable))
                .orElseGet(() -> authorRepository.findAll(pageable))
                .map(AuthorMapper.INSTANCE::toAuthorDto);
    }

    public AuthorDto findById(long id) {
        return authorRepository
                .findById(id)
                .map(AuthorMapper.INSTANCE::toAuthorDto)
                .orElseThrow(() -> new EntityNotFoundException("Author not found."));
    }

    @IsAdmin
    public AuthorDto create(AuthorRequestDto createAuthorDto) {
        Author author = AuthorMapper.INSTANCE.toAuthor(createAuthorDto);
        return AuthorMapper.INSTANCE.toAuthorDto(authorRepository.save(author));
    }

    @IsAdmin
    public AuthorDto update(long id, AuthorRequestDto authorRequestDto) {
        Author author = authorRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author not found."));

        // Update fields if they are present in the request body.
        Optional.ofNullable(authorRequestDto.name()).ifPresent(author::setName);
        Optional.ofNullable(authorRequestDto.surname()).ifPresent(author::setSurname);
        Optional.ofNullable(authorRequestDto.birthDate()).ifPresent(author::setBirthDate);
        Optional.ofNullable(authorRequestDto.deathDate()).ifPresent(author::setDeathDate);
        Optional.ofNullable(authorRequestDto.biography()).ifPresent(author::setBiography);

        return AuthorMapper.INSTANCE.toAuthorDto(authorRepository.save(author));
    }

    @IsAdmin
    public void delete(long id) {
        authorRepository.deleteById(id);
    }
}
