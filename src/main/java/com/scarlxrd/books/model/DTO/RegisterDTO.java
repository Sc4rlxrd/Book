package com.scarlxrd.books.model.DTO;

import com.scarlxrd.books.model.entity.Role;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record RegisterDTO(
        @NotBlank
        String email,
        @NotBlank
        String password,
        Set<Role> roles) {
}
