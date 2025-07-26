package com.scarlxrd.books.model.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class ClientRequestDTO {
    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Sobrenome é obrigatório")
    private String lastName;

    @NotBlank(message = "CPF é obrigatório")
    private String cpfNumber;

    @Valid // Valida cada item na lista de BookRequestDTO
    private List<BookRequestDTO> books; // Lista de livros associados ao cliente

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
