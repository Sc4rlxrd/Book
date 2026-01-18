package com.scarlxrd.books.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;
@Schema(
        name = "ClientResponse",
        description = "DTO de resposta com os dados completos do cliente"
)
public class ClientResponseDTO {
    @Schema(
            description = "Identificador único do cliente",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    private UUID id;
    @Schema(
            description = "Nome do cliente",
            example = "João"
    )
    private String name;
    @Schema(
            description = "Sobrenome do cliente",
            example = "Silva"
    )
    private String lastName;
    @Schema(
            description = "CPF do cliente formatado",
            example = "123.456.789-00"
    )
    private String cpf;
    @Schema(
            description = "Lista de livros cadastrados para o cliente"
    )
    private List<BookResponseDTO> books;

    public ClientResponseDTO() {
    }

    public ClientResponseDTO(UUID id, String name, String lastName, String cpf, List<BookResponseDTO> books) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.cpf = cpf;
        this.books = books;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public List<BookResponseDTO> getBooks() { return books; }
    public void setBooks(List<BookResponseDTO> books) { this.books = books; }
}

