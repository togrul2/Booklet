package com.github.togrul2.booklet.specifications;

import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface SpecificationAssembler<T> {
    Optional<Specification<T>> getSpecification();
}
