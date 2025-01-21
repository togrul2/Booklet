package com.github.togrul2.booklet.specifications;

import com.github.togrul2.booklet.dtos.book.BookFilterDto;
import com.github.togrul2.booklet.entities.Book;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Assembles specifications for the {@link Book} entity.
 * <p>
 * This class is used to create a {@link Specification} object for the {@link Book} entity based on the given
 * {@link BookFilterDto}.
 * The {@link Specification} object is used to filter the books in the database.
 * The {@link Specification} object is created by combining multiple specifications based on the given filter.
 * The specifications are created based on the title, author, genre, ISBN, and year of the book.
 * If the filter contains the title, author, genre, ISBN, or year, then the corresponding specification is created.
 * The specifications are combined using the AND operator.
 * If the filter does not contain the title, author, genre, ISBN, or year, then an empty specification is returned.
 * </p>
 */
@Builder
public class BookSpecificationAssembler extends SpecificationAssembler<Book> {
    private final BookFilterDto filterDto;

    private Optional<Specification<Book>> byTitle() {
        if (filterDto.title() != null) {
            return Optional.of(buildIlikeSearchSpecification("title", filterDto.title()));
        }
        return Optional.empty();
    }

    private Optional<Specification<Book>> byAuthor() {
        if (filterDto.authorId() != null) {
            return Optional.of(
                    (root, _, builder) ->
                            builder.equal(root.get("author").get("id"), filterDto.authorId())
            );
        }
        return Optional.empty();
    }

    private Optional<Specification<Book>> byGenre() {
        if (filterDto.genreId() != null) {
            return Optional.of(
                    (root, _, builder) -> builder.equal(root.get("genre").get("id"), filterDto.genreId())
            );
        }
        return Optional.empty();
    }

    private Optional<Specification<Book>> byIsbn() {
        if (filterDto.isbn() != null) {
            return Optional.of(
                    (root, _, builder) -> builder.equal(root.get("isbn"), filterDto.isbn())
            );
        }
        return Optional.empty();
    }

    private Optional<Specification<Book>> byYear() {
        if (filterDto.minYear() != null && filterDto.maxYear() != null) {
            return Optional.of((root, _, builder) ->
                    builder.between(root.get("year"), filterDto.minYear(), filterDto.maxYear()));
        } else if (filterDto.minYear() != null) {
            return Optional.of((root, _, builder) ->
                    builder.greaterThanOrEqualTo(root.get("year"), filterDto.minYear()));
        } else if (filterDto.maxYear() != null) {
            return Optional.of((root, _, builder) ->
                    builder.lessThanOrEqualTo(root.get("year"), filterDto.maxYear()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Specification<Book>> getSpecification() {
        List<Specification<Book>> specifications = new ArrayList<>();
        byTitle().ifPresent(specifications::add);
        byAuthor().ifPresent(specifications::add);
        byGenre().ifPresent(specifications::add);
        byIsbn().ifPresent(specifications::add);
        byYear().ifPresent(specifications::add);
        return specifications.stream().reduce(Specification::and);
    }
}
