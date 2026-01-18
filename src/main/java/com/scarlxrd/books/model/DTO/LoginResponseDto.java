package com.scarlxrd.books.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "LoginResponse",
        description = "Resposta da autenticação contendo o token JWT"
)
public record LoginResponseDto(
        @Schema(
                description = "Token JWT de autenticação",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String token) {
}
