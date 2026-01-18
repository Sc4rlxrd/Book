package com.scarlxrd.books.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RefreshTokenRequest",
        description = "DTO para renovação do token de acesso"
)
public record RefreshRequestDto(
        @Schema(
                description = "Refresh token válido",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String refreshToken) {
}
