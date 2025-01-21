package com.github.togrul2.booklet.specifications;

import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public abstract class SpecificationAssembler<T> {
    protected Specification<T> buildIlikeSearchSpecification(String fieldName, String value) {
        return (root, _, builder) ->
                builder.like(
                        builder.lower(root.get(fieldName)), "%" + value.toLowerCase() + "%"
                );
    }

    public abstract Optional<Specification<T>> getSpecification();
}
