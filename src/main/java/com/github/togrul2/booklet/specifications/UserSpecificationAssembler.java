package com.github.togrul2.booklet.specifications;

import com.github.togrul2.booklet.dtos.user.UserFilterDto;
import com.github.togrul2.booklet.entities.User;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
public class UserSpecificationAssembler extends SpecificationAssembler<User> {
    private final UserFilterDto filterDto;

    private Optional<Specification<User>> byFirstName() {
        if (filterDto.firstName() != null) {
            return Optional.of(buildIlikeSearchSpecification("firstName", filterDto.firstName()));
        }
        return Optional.empty();
    }

    private Optional<Specification<User>> byLastName() {
        if (filterDto.lastName() != null) {
            return Optional.of(buildIlikeSearchSpecification("lastName", filterDto.lastName()));
        }
        return Optional.empty();
    }

    private Optional<Specification<User>> byEmail() {
        if (filterDto.email() != null) {
            return Optional.of(buildIlikeSearchSpecification("email", filterDto.email()));
        }
        return Optional.empty();
    }

    private Optional<Specification<User>> byRole() {
        if (filterDto.role() != null) {
            return Optional.of(
                    (root, _, builder) -> builder.equal(root.get("role"), filterDto.role())
            );
        }
        return Optional.empty();
    }

    private Optional<Specification<User>> byActive() {
        if (filterDto.active() != null) {
            return Optional.of(
                    (root, _, builder) -> builder.equal(root.get("active"), filterDto.active())
            );
        }
        return Optional.empty();
    }

    private Optional<Specification<User>> byCreationDate() {
        if (filterDto.minCreationDatetime() != null && filterDto.maxCreationDatetime() != null) {
            return Optional.of((root, _, builder) ->
                    builder.between(
                            root.get("creationDatetime"),
                            filterDto.minCreationDatetime(),
                            filterDto.maxCreationDatetime()
                    )
            );
        } else if (filterDto.minCreationDatetime() != null) {
            return Optional.of((root, _, builder) ->
                    builder.greaterThanOrEqualTo(root.get("creationDatetime"), filterDto.minCreationDatetime()));
        } else if (filterDto.maxCreationDatetime() != null) {
            return Optional.of((root, _, builder) ->
                    builder.lessThanOrEqualTo(root.get("creationDatetime"), filterDto.maxCreationDatetime()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Specification<User>> getSpecification() {
        List<Specification<User>> specifications = new ArrayList<>();
        byFirstName().ifPresent(specifications::add);
        byLastName().ifPresent(specifications::add);
        byEmail().ifPresent(specifications::add);
        byRole().ifPresent(specifications::add);
        byActive().ifPresent(specifications::add);
        byCreationDate().ifPresent(specifications::add);
        return specifications.stream().reduce(Specification::and);
    }
}
