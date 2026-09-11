package com.eventbooking.bookingservice.messaging;

import java.time.Instant;

public record BookingEvent(
        Long bookingId,
        Long eventId,
        String customerEmail,
        Integer seatsBooked,
        String status,
        Instant occurredAt
) {
}
