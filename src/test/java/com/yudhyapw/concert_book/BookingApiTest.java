package com.yudhyapw.concert_book;

import com.yudhyapw.concert_book.dto.BookingRequest;
import com.yudhyapw.concert_book.dto.BookingResponse;
import com.yudhyapw.concert_book.dto.BookingTokenRequest;
import com.yudhyapw.concert_book.dto.BookingTokenResponse;
import com.yudhyapw.concert_book.entity.Event;
import com.yudhyapw.concert_book.entity.User;
import com.yudhyapw.concert_book.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookingApiTest extends IntegrationTest {

    @Autowired
    private RestTestClient client;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private BookingTokenRepository tokenRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private Long userId;
    private Long onSaleEventId;
    private Long closedEventId;
    private Long tinyEventId;

    @BeforeEach
    void seed() {
        bookingRepository.deleteAll();
        tokenRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        userId = userRepository.save(new User("Booker")).getId();
        OffsetDateTime now = OffsetDateTime.now();
        onSaleEventId = eventRepository.save(new Event("On Sale Fest", "Jakarta",
                now.plusDays(30), now.minusHours(1), now.plusHours(1), 100)).getId();
        closedEventId = eventRepository.save(new Event("Future Fest", "Jakarta",
                now.plusDays(30), now.plusDays(1), now.plusDays(2), 100)).getId();
        tinyEventId = eventRepository.save(new Event("Tiny Fest", "Jakarta",
                now.plusDays(30), now.minusHours(1), now.plusHours(1), 3)).getId();
    }

    private UUID issueToken(Long forEventId) {
        BookingTokenResponse token = client.post().uri("/api/booking-tokens")
                .body(new BookingTokenRequest(userId, forEventId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookingTokenResponse.class)
                .returnResult().getResponseBody();
        assertNotNull(token);
        return token.tokenId();
    }

    private BookingResponse submit(UUID tokenId, int quantity, int expectedStatus) {
        return client.post().uri("/api/bookings")
                .body(new BookingRequest(tokenId, userId, quantity))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody(BookingResponse.class)
                .returnResult().getResponseBody();
    }

    @Test
    void bookingHappyPathDecreasesStock() {
        BookingResponse booking = submit(issueToken(onSaleEventId), 2, 201);

        assertEquals(2, booking.quantity());
        client.get().uri("/api/events/" + onSaleEventId)
                .exchange()
                .expectBody().jsonPath("$.ticketAvailable").isEqualTo(98);
    }

    @Test
    void duplicateSubmitReplaysOriginalBooking() {
        UUID token = issueToken(onSaleEventId);

        BookingResponse first = submit(token, 2, 201);
        BookingResponse replayed = submit(token, 2, 200);   // 200, not 201

        assertEquals(first.bookingId(), replayed.bookingId());
        client.get().uri("/api/events/" + onSaleEventId)
                .exchange()
                .expectBody().jsonPath("$.ticketAvailable").isEqualTo(98);  // charged once
    }

    @Test
    void bookingOutsideSaleWindowIsRejected() {
        client.post().uri("/api/bookings")
                .body(new BookingRequest(issueToken(closedEventId), userId, 1))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void soldOutIsRejectedAndTokenStaysUsable() {
        // takes all 3 tickets
        submit(issueToken(tinyEventId), 3, 201);

        UUID lateToken = issueToken(tinyEventId);
        client.post().uri("/api/bookings")
                .body(new BookingRequest(lateToken, userId, 1))
                .exchange()
                .expectStatus().isEqualTo(409);

        // Same token again: still 409 (sold out), NOT a replay — proving the
        // failed attempt rolled the claim back and the token was never consumed.
        client.post().uri("/api/bookings")
                .body(new BookingRequest(lateToken, userId, 1))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void submittingSomeoneElsesTokenIsForbidden() {
        Long intruderId = userRepository.save(new User("Intruder")).getId();
        UUID victimToken = issueToken(onSaleEventId);

        client.post().uri("/api/bookings")
                .body(new BookingRequest(victimToken, intruderId, 1))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void unknownTokenReturns404() {
        client.post().uri("/api/bookings")
                .body(new BookingRequest(UUID.randomUUID(), userId, 1))
                .exchange()
                .expectStatus().isNotFound();
    }
}