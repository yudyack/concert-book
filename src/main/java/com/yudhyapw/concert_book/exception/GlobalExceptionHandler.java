package com.yudhyapw.concert_book.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        // 404
        return status(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<String> problems = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            problems.add(error.getField() + " " + error.getDefaultMessage());
        }
        // 400
        return status(HttpStatus.BAD_REQUEST, String.join("; ", problems));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadArgument(IllegalArgumentException e) {
        // 400
        return status(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(TokenOwnershipException.class)
    public ResponseEntity<ErrorResponse> handleOwnership(TokenOwnershipException e) {
        // 403
        return status(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(BookingConflictException e) {
        // 409
        return status(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BookingTokenRateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleConflict(BookingTokenRateLimitExceededException e) {
        // 429
        return status(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        return status(HttpStatus.CONFLICT, "conflicts with existing data");
    }

    private ResponseEntity<ErrorResponse> status(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), message));
    }
}