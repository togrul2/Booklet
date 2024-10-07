package com.github.togrul2.booklet.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, length = 50)
    private String slug;
}
