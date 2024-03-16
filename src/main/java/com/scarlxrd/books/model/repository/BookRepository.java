package com.scarlxrd.books.model.repository;

import com.scarlxrd.books.model.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book,Long> {
}
