package com.yudhyapw.concert_book.dto;

import com.yudhyapw.concert_book.entity.Booking;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(Long bookingId, UUID tokenId, Long userId, Long eventId,
                              int quantity, OffsetDateTime createdAt) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.getId(), booking.getToken().getId(),
                booking.getUser().getId(), booking.getEvent().getId(),
                booking.getQuantity(), booking.getCreatedAt());
    }
}