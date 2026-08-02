package com.yudhyapw.concert_book;

import com.yudhyapw.concert_book.dto.BookingTokenRequest;
import com.yudhyapw.concert_book.dto.BookingTokenResponse;
import com.yudhyapw.concert_book.entity.Event;
import com.yudhyapw.concert_book.entity.User;
import com.yudhyapw.concert_book.repository.*;
import com.yudhyapw.concert_book.service.BookingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.OffsetDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitApiTest extends IntegrationTest {

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
    @Autowired
    private BookingService bookingService;

    private Long userId;
    private Long limitedEventId;
    private Long otherEventId;

    @BeforeEach
    void seed() {
        bookingRepository.deleteAll();
        tokenRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        userId = userRepository.save(new User("Hammerer")).getId();
        OffsetDateTime now = OffsetDateTime.now();
        limitedEventId = eventRepository.save(new Event("Hot Drop", "Jakarta",
                now.plusDays(30), now.minusHours(1), now.plusHours(1), 100,
                2)).getId();
        otherEventId = eventRepository.save(new Event("Quiet Show", "Bandung",
                now.plusDays(30), now.minusHours(1), now.plusHours(1), 100, 
                1000)).getId();
    }

    private int requestToken(Long eventId) {
        return client.post().uri("/api/booking-tokens")
                .body(new BookingTokenRequest(userId, eventId))
                .exchange()
                .returnResult(String.class).getStatus().value();
    }

    @Test
    void thirdRapidTokenRequestIsThrottled() {
        assertEquals(201, requestToken(limitedEventId));
        assertEquals(201, requestToken(limitedEventId));
        assertEquals(429, requestToken(limitedEventId));
    }

    @Test
    void limitIsPerEventNotGlobal() {
        assertEquals(201, requestToken(limitedEventId));
        assertEquals(201, requestToken(limitedEventId));
        assertEquals(429, requestToken(limitedEventId));   
        assertEquals(201, requestToken(otherEventId));    
    }

    @Test
    void sustainsHundredUsersPerSecond() throws Exception {
        Long userId = userRepository.save(new User("waver")).getId();
        OffsetDateTime now = OffsetDateTime.now();
        Long eventId = eventRepository.save(new Event("Rate Wave", "Jakarta",
                now.plusDays(30), now.minusHours(1), now.plusHours(1), 10_000, 100)).getId();

        assertEquals(100, runBulk(userId, eventId, 500),
                "wave 1: 100 simultaneous requests within the configured rate must all pass");

        Thread.sleep(1000);   // next one-second window (bucket refilled)

        assertEquals(100, runBulk(userId, eventId, 500),
                "wave 2: the following second must sustain another 100 requests");
    }

    private int runBulk(Long userId, Long eventId, int size) throws Exception {
        AtomicInteger ok = new AtomicInteger();
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(size);
        try (ExecutorService pool = Executors.newFixedThreadPool(32)) {
            for (int i = 0; i < size; i++) {
                pool.submit(() -> {
                    try {
                        startGun.await();
                        bookingService.issueToken(new BookingTokenRequest(userId, eventId));
                        ok.incrementAndGet();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        finished.countDown();
                    }
                });
            }
            startGun.countDown();
            assertTrue(finished.await(30, TimeUnit.SECONDS));
        }
        return ok.get();
    }
}