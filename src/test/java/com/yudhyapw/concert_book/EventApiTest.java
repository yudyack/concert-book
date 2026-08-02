package com.yudhyapw.concert_book;

import com.yudhyapw.concert_book.entity.Event;
import com.yudhyapw.concert_book.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.OffsetDateTime;

@Testcontainers
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private RestTestClient client;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void seedEvents() {
        eventRepository.deleteAll();
        OffsetDateTime now = OffsetDateTime.now();
        eventRepository.save(new Event("Rock Festival", "Jakarta", now.plusDays(30),
                now.minusHours(1), now.plusHours(1), 100));
        eventRepository.save(new Event("Jazz Night", "Bandung", now.plusDays(45),
                now.plusDays(1), now.plusDays(2), 50));
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