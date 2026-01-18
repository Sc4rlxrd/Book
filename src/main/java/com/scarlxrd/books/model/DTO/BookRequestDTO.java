package com.scarlxrd.books.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "BookRequest",
        description = "DTO para cadastro de um livro"
)
public class BookRequestDTO {
    @Schema(
            description = "Título do livro",
            example = "Clean Code"
    )
    @NotBlank(message = "is mandatory")
    private String title;
    @Schema(
            description = "Autor do livro",
            example = "Robert C. Martin"
    )
    @NotBlank(message = "is mandatory")
    private String author;
    @Schema(
            description = "ISBN do livro",
            example = "9780132350884"
    )
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
