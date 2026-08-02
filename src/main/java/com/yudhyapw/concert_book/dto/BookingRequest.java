package com.yudhyapw.concert_book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BookingRequest(@NotNull UUID tokenId, @NotNull Long userId,
                             @NotNull @Min(1) Integer quantity) {
}