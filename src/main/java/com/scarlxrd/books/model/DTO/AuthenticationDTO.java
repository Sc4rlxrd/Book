package com.scarlxrd.books.model.DTO;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationDTO(
        @NotBlank
        String email,
        @NotBlank
        String password) {
}
