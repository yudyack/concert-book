package com.yudhyapw.concert_book.dto;

import com.yudhyapw.concert_book.entity.Event;
import java.time.OffsetDateTime;

public record EventResponse(Long eventId, String name, String venue, OffsetDateTime eventTime,
                            OffsetDateTime saleStart, OffsetDateTime saleEnd,
                            int ticketTotal, int ticketAvailable) {

    public static EventResponse from(Event event) {
        return new EventResponse(event.getId(), event.getName(), event.getVenue(),
                event.getEventTime(), event.getSaleStart(), event.getSaleEnd(),
                event.getTicketTotal(), event.getTicketAvailable());
    }
}