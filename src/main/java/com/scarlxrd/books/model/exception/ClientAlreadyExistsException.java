package com.scarlxrd.books.model.exception;

public class ClientAlreadyExistsException extends BusinessException{
    public ClientAlreadyExistsException(String message) {
        super(message);
    }
}
