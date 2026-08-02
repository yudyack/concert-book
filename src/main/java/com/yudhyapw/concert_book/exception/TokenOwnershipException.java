package com.yudhyapw.concert_book.exception;

// Status code FORBIDDEN 403
public class TokenOwnershipException extends RuntimeException {

    public TokenOwnershipException(String message) {
        super(message);
    }
}