
package com.yudhyapw.concert_book.exception;

// Status code TOO_MANY_REQUEST 429
public class BookingTokenRateLimitExceededException extends RuntimeException {

    public BookingTokenRateLimitExceededException (String message) {
        super(message);
    }
}