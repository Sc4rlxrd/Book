package com.scarlxrd.books.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "TokenResponse",
        description = "Resposta contendo access token e refresh token"
)
public record TokenResponseDto(
        @Schema(
                description = "Token de acesso JWT",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String accessToken,
        @Schema(
                description = "Token para renovação do access token",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String refreshToken) {
}
