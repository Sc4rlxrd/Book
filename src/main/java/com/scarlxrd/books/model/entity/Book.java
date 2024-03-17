package com.scarlxrd.books.model.entity;

import com.scarlxrd.books.model.DTO.BookDTO;
import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String gender;
    private String year;
    @ManyToOne
    private Client client;

    public Book() {
    }

    public Book(Long id, String name, String description, String gender, String year, Client client) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.gender = gender;
        this.year = year;
        this.client = client;
    }
    public Book(BookDTO bookDTO) {
        this.name = bookDTO.name();
        this.description = bookDTO.description();
        this.gender =bookDTO.gender();
        this.year = bookDTO.year();
        this.id = bookDTO.id();
        this.client = bookDTO.client();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
