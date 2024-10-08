package com.github.togrul2.booklet.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, length = 50)
    private String surname;
    @Column(nullable = false)
    private Date birthDate;
    private Date deathDate;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String biography;
    @Check(constraints = "rating >= 0 AND rating <= 5", name = "rating_validness_check")
    @Column(precision = 2, scale = 1)
    private BigDecimal rating;
}
