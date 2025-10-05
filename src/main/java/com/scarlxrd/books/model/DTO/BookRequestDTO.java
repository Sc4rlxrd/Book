package com.scarlxrd.books.model.DTO;

import jakarta.validation.constraints.NotBlank;
public class BookRequestDTO {
    @NotBlank(message = "is mandatory")
    private String title;
    @NotBlank(message = "is mandatory")
    private String author;
    @NotBlank(message = "is mandatory")
    private String isbn;


    public BookRequestDTO() {}

    public BookRequestDTO(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
}
