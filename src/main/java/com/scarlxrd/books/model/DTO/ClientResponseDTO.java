package com.scarlxrd.books.model.DTO;

import java.util.List;
import java.util.UUID;

public class ClientResponseDTO {
    private UUID id;
    private String name;
    private String lastName;
    private String cpf; // Já formatado pelo @JsonValue no Cpf.java
    private List<BookResponseDTO> books; // Lista de DTOs de livros

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

