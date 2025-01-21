package com.github.togrul2.booklet.specifications;

import com.github.togrul2.booklet.dtos.author.AuthorFilterDto;
import com.github.togrul2.booklet.entities.Author;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
public class AuthorSpecificationAssembler extends SpecificationAssembler<Author> {
    private final AuthorFilterDto filterDto;

    private Optional<Specification<Author>> byName() {
        if (filterDto.name() != null)
            return Optional.of((root, _, builder) ->
                    builder.like(builder.lower(root.get("name")), "%" + filterDto.name().toLowerCase() + "%"));
        return Optional.empty();
    }

    private Optional<Specification<Author>> bySurname() {
        if (filterDto.surname() != null)
            return Optional.of((root, _, builder) ->
                    builder.like(builder.lower(root.get("surname")), "%" + filterDto.surname().toLowerCase() + "%"));
        return Optional.empty();
    }

    private Optional<Specification<Author>> byBirthDate() {
        if (filterDto.minBirthDate() != null && filterDto.maxBirthDate() != null)
            return Optional.of((root, _, builder) ->
                    builder.between(root.get("birthDate"), filterDto.minBirthDate(), filterDto.maxBirthDate()));
        else if (filterDto.minBirthDate() != null)
            return Optional.of((root, _, builder) ->
                    builder.greaterThanOrEqualTo(root.get("birthDate"), filterDto.minBirthDate()));
        else if (filterDto.maxBirthDate() != null)
            return Optional.of((root, _, builder) ->
                    builder.lessThanOrEqualTo(root.get("birthDate"), filterDto.maxBirthDate()));
        return Optional.empty();
    }

    private Optional<Specification<Author>> byDeathDate() {
        if (filterDto.minDeathDate() != null && filterDto.maxDeathDate() != null)
            return Optional.of((root, _, builder) ->
                    builder.between(root.get("deathDate"), filterDto.minDeathDate(), filterDto.maxDeathDate()));
        else if (filterDto.minDeathDate() != null)
            return Optional.of((root, _, builder) ->
                    builder.greaterThanOrEqualTo(root.get("deathDate"), filterDto.minDeathDate()));
        else if (filterDto.maxDeathDate() != null)
            return Optional.of((root, _, builder) ->
                    builder.lessThanOrEqualTo(root.get("deathDate"), filterDto.maxDeathDate()));
        return Optional.empty();
    }

    public Optional<Specification<Author>> getSpecification() {
        List<Specification<Author>> specifications = new ArrayList<>();
        byName().ifPresent(specifications::add);
        bySurname().ifPresent(specifications::add);
        byBirthDate().ifPresent(specifications::add);
        byDeathDate().ifPresent(specifications::add);
        return specifications.stream().reduce(Specification::and);
    }
}
