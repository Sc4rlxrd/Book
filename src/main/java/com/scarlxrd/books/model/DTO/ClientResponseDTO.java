package com.scarlxrd.books.model.DTO;

import com.scarlxrd.books.model.entity.Client;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientResponseDTO {
    private UUID id;
    private String name;
    private String lastName;
    private String cpf; // Já formatado pelo @JsonValue no Cpf.java
    private List<BookResponseDTO> books; // Lista de DTOs de livros

    public ClientResponseDTO(Client client) {
        this.id = client.getId();
        this.name = client.getName();
        this.lastName = client.getLastName();
        this.cpf = client.getCpf() != null ? client.getCpf().toString() : null; // Usa o toString() formatado
        this.books = client.getBooks().stream()
                .map(BookResponseDTO::new) // Converte cada Book para BookResponseDTO
                .collect(Collectors.toList());
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

