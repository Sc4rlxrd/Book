package com.scarlxrd.books.model.exception;

public class ClientNotFoundException extends RuntimeException{
    public ClientNotFoundException(String message) {
        super(message);
    }
}
