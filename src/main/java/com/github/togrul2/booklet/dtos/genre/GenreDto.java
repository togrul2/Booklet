package com.github.togrul2.booklet.dtos.genre;

import java.io.Serializable;

public record GenreDto(long id, String name, String slug) implements Serializable {
}
