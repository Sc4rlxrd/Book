package com.scarlxrd.books.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clients") // Nome da tabela no banco de dados
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private String lastName;
    @Embedded
    private Cpf cpf;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference // Usado para evitar ‘loop’ infinito na serialização JSON
    private List<Book> books = new ArrayList<>(); // Inicialize para evitar NullPointerException

    // Construtor padrão (sem argumentos) é OBRIGATÓRIO para entidades JPA
    public Client() {}

    // Construtor para criar um cliente
    public Client(UUID id, String name, String lastName, Cpf cpf) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.cpf = cpf;
    }


    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Cpf getCpf() {
        return cpf;
    }

    public void setCpf(Cpf cpf) {
        this.cpf = cpf;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }


    // Mét0do auxiliar para adicionar um livro e manter a consistência da relação bidirecional
    public void addBook(Book book) {
        books.add(book);
        book.setClient(this);
    }

    // Mét0do auxiliar para remover um livro
    public void removeBook(Book book) {
        books.remove(book);
        book.setClient(null);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", cpf=" + cpf +
                ", books=" + books.size() + " books" + // Evite imprimir a lista completa de livros para não criar loop infinito
                '}';
    }
}