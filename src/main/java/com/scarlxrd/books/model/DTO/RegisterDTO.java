package com.scarlxrd.books.model.DTO;

import com.scarlxrd.books.model.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;
@Schema(
        name = "RegisterRequest",
        description = "DTO para registro de novos usuários"
)
public record RegisterDTO(

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
        String password,
        @Schema(
                description = "Perfis do usuário",
                example = "[\"USER\", \"ADMIN\"]"
        )
        Set<Role> roles) {
}
