package com.scarlxrd.books.model.controller;

import com.scarlxrd.books.model.exception.ClientAlreadyExistsException;
import com.scarlxrd.books.model.exception.ClientNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando argumento é inválido")
    void shouldHandleIllegalArgument() {
        ProblemDetail problem = handler.handleIllegalArgument(new IllegalArgumentException("cpf inválido"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Invalid argument", problem.getTitle());
        assertEquals("cpf inválido", problem.getDetail());
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found quando cliente não é encontrado")
    void shouldHandleClientNotFound() {
        ProblemDetail problem = handler.handleClientNotFound(new ClientNotFoundException("Cliente X não encontrado"));

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Client not found", problem.getTitle());
        assertEquals("Cliente X não encontrado", problem.getDetail());
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict quando cliente já existe")
    void shouldHandleClientAlreadyExists() {
        ProblemDetail problem = handler.handleClientAlreadyExists(new ClientAlreadyExistsException("Cliente já existe"));

        assertEquals(HttpStatus.CONFLICT.value(), problem.getStatus());
        assertEquals("Client already exists", problem.getTitle());
        assertEquals("Cliente já existe", problem.getDetail());
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando credenciais são inválidas")
    void shouldHandleBadCredentials() {
        ProblemDetail problem = handler.handleBadCredentials(new BadCredentialsException("senha errada"));

        assertEquals(HttpStatus.UNAUTHORIZED.value(), problem.getStatus());
        assertEquals("Unauthorized", problem.getTitle());
        assertEquals("Email ou senha inválidos", problem.getDetail());
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden quando acesso é negado")
    void shouldHandleAccessDenied() {
        ProblemDetail problem = handler.handleAccessDenied(new AccessDeniedException("sem permissão"));

        assertEquals(HttpStatus.FORBIDDEN.value(), problem.getStatus());
        assertEquals("Forbidden", problem.getTitle());
        assertEquals("Você não tem permissão para acessar este recurso", problem.getDetail());
    }

    @Test
    @DisplayName("Deve retornar 500 Internal Server Error quando ocorre erro genérico")
    void shouldHandleGenericException() {
        ProblemDetail problem = handler.handleGeneric(new Exception("erro interno"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("Internal server error", problem.getTitle());
        assertEquals("Ocorreu um erro inesperado", problem.getDetail());
    }

}