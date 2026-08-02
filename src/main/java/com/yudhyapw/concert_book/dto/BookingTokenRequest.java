package com.yudhyapw.concert_book.dto;

import jakarta.validation.constraints.NotNull;

public record BookingTokenRequest(@NotNull Long userId, @NotNull Long eventId) {
}