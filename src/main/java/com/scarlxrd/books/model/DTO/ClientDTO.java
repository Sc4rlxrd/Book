package com.scarlxrd.books.model.DTO;

import com.scarlxrd.books.model.entity.Book;

import java.util.List;

public record ClientDTO(String name, Integer age, List<Book> books, Long id, Integer password) {

}
