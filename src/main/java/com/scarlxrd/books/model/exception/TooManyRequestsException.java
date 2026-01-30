package com.scarlxrd.books.model.exception;

import lombok.Getter;

@Getter
public class TooManyRequestsException extends RuntimeException {

    private final long seconds;
    public TooManyRequestsException(String message, long seconds) {
        super(message);
        this.seconds = seconds;
    }
}
