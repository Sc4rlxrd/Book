package com.scarlxrd.books.model.entity;

import com.scarlxrd.books.model.DTO.BookDTO;
import com.scarlxrd.books.model.DTO.ClientDTO;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "tb_clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer age;
    private Integer password;
    @OneToMany
    private List<Book> books;



    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public Client() {
    }

    public Client(Long id, String name, Integer age, Integer password, List<Book> books ) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.password = password;
        this.books = books;

    }
    public  Client(ClientDTO clientDTO){
        this.name = clientDTO.name();
        this.age = clientDTO.age();
        this.password = clientDTO.password();
        this.books = clientDTO.books();
        this.id = clientDTO.id();


    }


    public Integer getPassword() {
        return password;
    }

    public void setPassword(Integer password) {
        this.password = password;
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
