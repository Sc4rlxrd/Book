package com.scarlxrd.books.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.scarlxrd.books.model.entity.Client;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;
    private String author;
    private String isbn; // International Standard Book Number

    @ManyToOne // Muitos livros para um cliente
    @JoinColumn(name = "client_id", nullable = false) // Coluna de chave estrangeira na tabela 'books'
    @JsonBackReference // Usado para evitar ‘loop’ infinito na serialização JSON
    private Client client; // O cliente ao qual este livro pertence

    // Construtor padrão (sem argumentos) é OBRIGATÓRIO para entidades JPA
    public Book() {}

    // Construtor para facilitar a criação de objetos Book
    public Book(String title, String author, String isbn, Client client) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.client = client;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                // Evita referenciar o cliente diretamente para não criar loop no toString
                '}';
    }
}
