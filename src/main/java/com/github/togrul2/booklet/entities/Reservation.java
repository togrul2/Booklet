package com.github.togrul2.booklet.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@ToString(exclude = {"user", "book"})
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private Book book;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
