package com.yudhyapw.concert_book.service;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-event token-bucket rate limiter guarding booking-token issuance.
 */
@Component
public class BookingTokenRateLimiter {

    private final ConcurrentHashMap<Long, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryAcquire(Long eventId, int ratePerSecond) {
        Bucket bucket = buckets.computeIfAbsent(eventId, id -> Bucket.builder()
                .addLimit(limit -> limit.capacity(ratePerSecond)
                        .refillIntervally(ratePerSecond, Duration.ofSeconds(1)))
                .build());
        return bucket.tryConsume(1);
    }
}