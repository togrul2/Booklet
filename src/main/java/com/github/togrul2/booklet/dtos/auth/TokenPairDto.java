package com.github.togrul2.booklet.dtos.auth;

import lombok.Builder;

@Builder
public record TokenPairDto(
        String accessToken,
        String refreshToken
) {
}
