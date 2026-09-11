package com.eventbooking.bookingservice.exception;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String message,
        Instant timestamp
) {
}
