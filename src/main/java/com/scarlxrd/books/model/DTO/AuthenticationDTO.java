package com.scarlxrd.books.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
@Schema(
        name = "AuthenticationRequest",
        description = "DTO para autenticação do usuário"
)
public record AuthenticationDTO(
        @Schema(
                description = "E-mail do usuário",
                example = "scarlxrd@teste.com"
        )
        @NotBlank
        String email,
        @Schema(
                description = "Senha do usuário",
                example = "123456"
        )
        @NotBlank
        String password) {
}
