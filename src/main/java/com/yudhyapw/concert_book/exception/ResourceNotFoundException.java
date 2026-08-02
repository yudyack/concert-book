package com.yudhyapw.concert_book.exception;

// Status code NOT_FOUND 404
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}