package com.yudhyapw.concert_book.exception;

/** Business-rule rejection: sale window closed or tickets sold out. */
// Status code CONFLICT 409
public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String message) {
        super(message);
    }
}