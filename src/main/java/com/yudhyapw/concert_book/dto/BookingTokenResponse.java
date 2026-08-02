package com.yudhyapw.concert_book.dto;

import com.yudhyapw.concert_book.entity.BookingToken;
import java.util.UUID;

public record BookingTokenResponse(UUID tokenId, Long userId, Long eventId, String status) {

    public static BookingTokenResponse from(BookingToken token) {
        return new BookingTokenResponse(token.getId(), token.getUser().getId(),
                token.getEvent().getId(), token.getStatus().name());
    }
}