package com.scarlxrd.books.model.controller;

import com.scarlxrd.books.model.DTO.BookDTO;
import com.scarlxrd.books.model.entity.Book;
import com.scarlxrd.books.model.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @RequestMapping(value = "/v2/create", method = RequestMethod.POST)
    public ResponseEntity<Book> createBook(@RequestBody BookDTO bookDTO){
        Book newBook = bookService.createBook(bookDTO);
        return new ResponseEntity<>(newBook, HttpStatus.CREATED);
    }
    @RequestMapping(value = "/v2/get", method = RequestMethod.GET)
    public List<Book> get(){
        return bookService.get();
    }
    @RequestMapping(value = "/v2/update/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO){
        return bookService.updateBook(id, bookDTO);
    }

}
