package com.yudhyapw.concert_book.service;

import com.yudhyapw.concert_book.dto.BookingRequest;
import com.yudhyapw.concert_book.dto.BookingResponse;
import com.yudhyapw.concert_book.dto.BookingTokenRequest;
import com.yudhyapw.concert_book.dto.BookingTokenResponse;
import com.yudhyapw.concert_book.entity.*;
import com.yudhyapw.concert_book.exception.*;
import com.yudhyapw.concert_book.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final BookingTokenRepository tokenRepository;
    private final BookingRepository bookingRepository;

    public BookingService(UserRepository userRepository, EventRepository eventRepository,
            BookingTokenRepository tokenRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.tokenRepository = tokenRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public BookingTokenResponse issueToken(BookingTokenRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("user " + request.userId() + " not found"));
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("event " + request.eventId() + " not found"));
        BookingToken token = tokenRepository.save(new BookingToken(user, event));
        return BookingTokenResponse.from(token);
    }

    /**
     * Submits a booking for a previously issued token. 
     */
    @Transactional
    public SubmitResult submitBooking(BookingRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        BookingToken token = tokenRepository.findById(request.tokenId())
                .orElseThrow(() -> new ResourceNotFoundException("token " + request.tokenId() + " not found"));
                
        if (!token.getUser().getId().equals(request.userId())) {
            throw new TokenOwnershipException("token does not belong to user " + request.userId());
        }
        if (token.getStatus() == TokenStatus.USED) {
            return new SubmitResult(replay(request.tokenId()), false);
        }

        Event event = token.getEvent();
        if (!event.isOnSaleAt(now)) {
            throw new BookingConflictException("event is not on sale at this time");
        }
        // Atomic claim
        if (tokenRepository.claim(request.tokenId(), request.userId(), now) == 0) {
            return new SubmitResult(replay(request.tokenId()), false);
        }
        if (eventRepository.decrementAvailableTickets(event.getId(), request.quantity()) == 0) {
            throw new BookingConflictException("not enough tickets available");
        }

        Booking booking = bookingRepository.save(
                new Booking(token, token.getUser(), event, request.quantity()));
        return new SubmitResult(BookingResponse.from(booking), true);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findBookingsByUser(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    private BookingResponse replay(UUID tokenId) {
        return bookingRepository.findByTokenId(tokenId)
                .map(BookingResponse::from)
                .orElseThrow(() -> new IllegalStateException("used token " + tokenId + " has no booking"));
    }

    /** Marks whether the booking was created now (201) or replayed (200). */
    public record SubmitResult(BookingResponse booking, boolean created) {
    }
}