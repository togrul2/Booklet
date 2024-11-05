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
public class Genre {
    public final static int NAME_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = NAME_MAX_LENGTH, unique = true)
    private String name;
    @Column(nullable = false, length = NAME_MAX_LENGTH, unique = true)
    private String slug;
}
