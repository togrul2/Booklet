package com.github.togrul2.booklet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    public static final int TITLE_MAX_LENGTH = 100;
    public static final int ISBN_MAX_LENGTH = 13;
    public static final int ISBN_MIN_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;
    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;
    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;
    @Column(unique = true, length = ISBN_MAX_LENGTH)
    private String isbn;
    @Column(nullable = false)
    private int year;
}
