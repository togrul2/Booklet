package com.github.togrul2.booklet.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class PaginationDto {
    @Min(1)
    private final int pageNumber = 1;
    @Min(1)
    @Max(100)
    private final int pageSize = 10;
}
