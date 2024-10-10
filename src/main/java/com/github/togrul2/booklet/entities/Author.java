package com.github.togrul2.booklet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Author {
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_SURNAME_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;
    @Column(nullable = false, length = MAX_SURNAME_LENGTH)
    private String surname;
    @Column(nullable = false)
    private LocalDate birthDate;
    private LocalDate deathDate;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String biography;
}
