package com.yudhyapw.concert_book.controller;

import com.yudhyapw.concert_book.dto.*;
import com.yudhyapw.concert_book.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/booking-tokens")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingTokenResponse issueToken(@Valid @RequestBody BookingTokenRequest request) {
        return bookingService.issueToken(request);
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> submitBooking(@Valid @RequestBody BookingRequest request) {
        BookingService.SubmitResult result = bookingService.submitBooking(request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.booking());
    }

    @GetMapping("/bookings")
    public List<BookingResponse> findByUser(@RequestParam Long userId) {
        return bookingService.findBookingsByUser(userId);
    }
}