package com.scarlxrd.books.model.DTO;

import com.scarlxrd.books.model.entity.Book;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;
@Schema(
        name = "BookResponse",
        description = "DTO de resposta com os dados do livro"
)
public class BookResponseDTO {
    @Schema(
            description = "Identificador único do livro",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    private UUID id;
    @Schema(
            description = "Título do livro",
            example = "Clean Code"
    )
    private String title;

    @Schema(
            description = "Autor do livro",
            example = "Robert C. Martin"
    )
    private String author;

    @Schema(
            description = "ISBN do livro",
            example = "9780132350884"
    )
    private String isbn;

    public BookResponseDTO() {
    }

    public BookResponseDTO(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.isbn = book.getIsbn();
    }

    public BookResponseDTO(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
}
