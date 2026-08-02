package com.yudhyapw.concert_book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateEventRequest(@NotBlank String name,
                                 String venue,
                                 OffsetDateTime eventTime,
                                 @NotNull OffsetDateTime saleStart,
                                 @NotNull OffsetDateTime saleEnd,
                                 @NotNull @Min(1) Integer ticketTotal,
                                 @NotNull @Min(1) Integer rateLimitPerSecond) {
}