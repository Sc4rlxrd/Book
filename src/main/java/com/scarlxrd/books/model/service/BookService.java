package com.scarlxrd.books.model.service;

import com.scarlxrd.books.model.DTO.BookDTO;
import com.scarlxrd.books.model.DTO.ClientDTO;
import com.scarlxrd.books.model.entity.Book;
import com.scarlxrd.books.model.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> get(){
        return  bookRepository.findAll();
    }

    public Book createBook(BookDTO bookDTO ){
        Book newBook = new Book(bookDTO);
        return bookRepository.save(newBook);
    }

    public ResponseEntity<Book> updateBook(Long id , BookDTO bookDTO){
        Optional<Book> oldBook = bookRepository.findById(id);
        if(oldBook.isPresent()){
            Book book = oldBook.get();
            book.setName(bookDTO.name());
            book.setDescription(bookDTO.description());
            book.setYear(bookDTO.year());
            book.setGender(bookDTO.gender());
            bookRepository.save(book);
            return  new ResponseEntity<>(HttpStatus.OK);
        }else{
            return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
