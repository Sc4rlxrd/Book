package com.scarlxrd.books.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(
        name = "ClientRequest",
        description = "DTO para criação de um cliente com seus livros favoritos"
)
public class ClientRequestDTO {

    @Schema(
            description = "Nome do cliente",
            example = "João"
    )
    @NotBlank(message = "is mandatory")
    private String name;
    @Schema(
            description = "Sobrenome do cliente",
            example = "Silva"
    )
    @NotBlank(message = "is mandatory")
    private String lastName;
    @Schema(
            description = "CPF do cliente (somente números ou formatado)",
            example = "12345678900"
    )
    @NotBlank(message = "is mandatory")
    private String cpfNumber;
    @Schema(
            description = "Lista de livros favoritos do cliente"
    )
    @Valid
    private List<BookRequestDTO> books;

    // Construtores, Getters e Setters
    public ClientRequestDTO() {}

    public ClientRequestDTO(String name, String lastName, String cpfNumber, List<BookRequestDTO> books) {
        this.name = name;
        this.lastName = lastName;
        this.cpfNumber = cpfNumber;
        this.books = books;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getCpfNumber() { return cpfNumber; }
    public void setCpfNumber(String cpfNumber) { this.cpfNumber = cpfNumber; }
    public List<BookRequestDTO> getBooks() { return books; }
    public void setBooks(List<BookRequestDTO> books) { this.books = books; }
}
