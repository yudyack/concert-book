package com.yudhyapw.concert_book.entity;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    private final OffsetDateTime saleStart = OffsetDateTime.parse("2026-08-10T10:00:00+07:00");
    private final OffsetDateTime saleEnd = OffsetDateTime.parse("2026-08-10T10:20:00+07:00");

    private Event event() {
        return new Event("Concert", "Jakarta", saleEnd.plusDays(30), saleStart, saleEnd, 10_000);
    }

    @Test
    void constructorStartsWithAllTicketsAvailable() {
        assertEquals(10_000, event().getTicketAvailable());
    }

    @Test
    void notOnSaleBeforeWindowOpens() {
        assertFalse(event().isOnSaleAt(saleStart.minusSeconds(1)));
    }

    @Test
    void onSaleAtExactOpeningMoment() {
        assertTrue(event().isOnSaleAt(saleStart));
    }

    @Test
    void onSaleInsideWindow() {
        assertTrue(event().isOnSaleAt(saleStart.plusMinutes(10)));
    }

    @Test
    void onSaleAtExactClosingMoment() {
        assertTrue(event().isOnSaleAt(saleEnd));
    }

    @Test
    void notOnSaleAfterWindowCloses() {
        assertFalse(event().isOnSaleAt(saleEnd.plusSeconds(1)));
    }
}