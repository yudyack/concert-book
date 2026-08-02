package com.yudhyapw.concert_book;

import com.yudhyapw.concert_book.entity.Event;
import com.yudhyapw.concert_book.repository.BookingRepository;
import com.yudhyapw.concert_book.repository.BookingTokenRepository;
import com.yudhyapw.concert_book.repository.EventRepository;
import com.yudhyapw.concert_book.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.OffsetDateTime;

class EventApiTest extends IntegrationTest {

    @Autowired
    private RestTestClient client;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingTokenRepository tokenRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void seedEvents() {
        bookingRepository.deleteAll();
        tokenRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        eventRepository.deleteAll();
        OffsetDateTime now = OffsetDateTime.now();
        eventRepository.save(new Event("Rock Festival", "Jakarta", now.plusDays(30),
                now.minusHours(1), now.plusHours(1), 100, 100));
        eventRepository.save(new Event("Jazz Night", "Bandung", now.plusDays(45),
                now.plusDays(1), now.plusDays(2), 50, 100));
    }

    @Test
    void searchWithoutFilterReturnsAllEvents() {
        client.get().uri("/api/events")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void searchByNameFiltersCaseInsensitively() {
        client.get().uri("/api/events?name=rock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Rock Festival");
    }

    @Test
    void unknownEventReturns404() {
        client.get().uri("/api/events/999999")
                .exchange()
                .expectStatus().isNotFound();
    }
}