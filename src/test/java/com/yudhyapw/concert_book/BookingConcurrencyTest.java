package com.yudhyapw.concert_book;

import com.yudhyapw.concert_book.dto.BookingRequest;
import com.yudhyapw.concert_book.dto.BookingTokenRequest;
import com.yudhyapw.concert_book.entity.Event;
import com.yudhyapw.concert_book.entity.User;
import com.yudhyapw.concert_book.exception.BookingConflictException;
import com.yudhyapw.concert_book.repository.*;
import com.yudhyapw.concert_book.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BookingConcurrencyTest extends IntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private BookingTokenRepository tokenRepository;
    @Autowired
    private BookingRepository bookingRepository;


    @BeforeEach
    void seed() {
        bookingRepository.deleteAll();
        tokenRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void raceForLimitedStockNeverOversells() throws Exception {

        int tickets = 5_000;
        int competitors = 10_000;
        int rateLimitPerSecond = 10_000;
        OffsetDateTime now = OffsetDateTime.now();
        long eventId = eventRepository.save(new Event("Ticket War", "Jakarta",
                now.plusDays(30), now.minusMinutes(10), now.plusMinutes(10), tickets, rateLimitPerSecond)).getId();

        List<UUID> tokens = new ArrayList<>();
        for (int i = 0; i < competitors; i++) {
            User user = userRepository.save(new User("racer-" + i));
            tokens.add(bookingService.issueToken(
                    new BookingTokenRequest(user.getId(), eventId)).tokenId());
        }
        List<Long> userIds = userRepository.findAll().stream().map(User::getId).sorted().toList();

        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(competitors);
        long timerStart;
        long elapsed;

        try (ExecutorService pool = Executors.newFixedThreadPool(32)) {
            for (int i = 0; i < competitors; i++) {
                UUID token = tokens.get(i);
                Long userId = userIds.get(i);
                pool.submit(() -> {
                    try {
                        start.await();
                        bookingService.submitBooking(new BookingRequest(token, userId, 1));
                        accepted.incrementAndGet();
                    } catch (BookingConflictException e) {
                        rejected.incrementAndGet();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        finished.countDown();
                    }
                });
            }
            start.countDown();
            timerStart = System.nanoTime(); 
            assertTrue(finished.await(60, TimeUnit.SECONDS), "race did not finish in time");
            elapsed = (System.nanoTime() - timerStart) / 1_000_000;
        } 

        System.out.printf("elapsed = %d", elapsed);

        assertEquals(tickets, accepted.get(), "exactly the available tickets must be sold");
        assertEquals(competitors - tickets, rejected.get());
        assertEquals(tickets, bookingRepository.count());
        assertEquals(0, eventRepository.findById(eventId).orElseThrow().getTicketAvailable());


    }

    /**
     * One token submitted 20 times at the same instant.
     */
    @Test
    void duplicateSubmissionRaceCreatesExactlyOneBooking() throws Exception {

        int tickets = 10;
        OffsetDateTime now = OffsetDateTime.now();
        long eventId = eventRepository.save(new Event("Ticket War", "Jakarta",
                now.plusDays(30), now.minusMinutes(10), now.plusMinutes(10), tickets, 100)).getId();


        Long userId = userRepository.save(new User("double-clicker")).getId();
        UUID token = bookingService.issueToken(new BookingTokenRequest(userId, eventId)).tokenId();

        int attempts = 20;
        AtomicInteger created = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(attempts);

        try (ExecutorService pool = Executors.newFixedThreadPool(attempts)) {
            for (int i = 0; i < attempts; i++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        if (bookingService.submitBooking(new BookingRequest(token, userId, 3)).created()) {
                            created.incrementAndGet();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        finished.countDown();
                    }
                });
            }
            start.countDown();
            assertTrue(finished.await(60, TimeUnit.SECONDS));
        }

        assertEquals(1, created.get(), "exactly one submission may create the booking");
        assertEquals(1, bookingRepository.count());
        assertEquals(tickets - 3, eventRepository.findById(eventId).orElseThrow().getTicketAvailable());
    }
}