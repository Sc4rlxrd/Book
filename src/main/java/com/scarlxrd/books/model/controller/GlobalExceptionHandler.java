package com.scarlxrd.books.model.controller;

import com.scarlxrd.books.model.exception.ClientAlreadyExistsException;
import com.scarlxrd.books.model.exception.ClientNotFoundException;
import com.scarlxrd.books.model.exception.TooManyRequestsException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Tag(name = "Errors", description = "Modelo de erros da API")
@ControllerAdvice
public class GlobalExceptionHandler {

    // 400 - IllegalArgumentException (argumentos inválidos)
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid argument");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    // 400 - Erros de validação do @Valid (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");
        problem.setDetail("Um ou mais campos são inválidos");

        // adiciona campos inválidos no corpo
        ex.getBindingResult().getFieldErrors().forEach(err ->
                problem.setProperty(err.getField(), err.getDefaultMessage())
        );

        return problem;
    }

    // 404 - Cliente não encontrado
    @ExceptionHandler(ClientNotFoundException.class)
    public ProblemDetail handleClientNotFound(ClientNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Client not found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    // 409 - Cliente já existe
    @ExceptionHandler(ClientAlreadyExistsException.class)
    public ProblemDetail handleClientAlreadyExists(ClientAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Client already exists");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    // 401 - Credenciais inválidas
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized");
        problem.setDetail("Email ou senha inválidos");
        return problem;
    }

    // 403 - Acesso negado
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Forbidden");
        problem.setDetail("Você não tem permissão para acessar este recurso");
        return problem;
    }
    // 429 - enviou muitas solicitações num curto período.
    @ExceptionHandler(TooManyRequestsException.class)
    public ProblemDetail handleTooManyRequests(TooManyRequestsException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
        problem.setTitle("Too Many Requests");
        problem.setDetail(ex.getMessage());
        problem.setProperty("retry_after_seconds", ex.getSeconds());
        return problem;
    }

    // 500 - Erros não tratados
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal server error");
        problem.setDetail("Ocorreu um erro inesperado");
        return problem;
    }
}
