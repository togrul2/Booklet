package com.github.togrul2.booklet.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@ToString(exclude = "user")
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String token;
    private boolean active;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
