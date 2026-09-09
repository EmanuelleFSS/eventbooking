package com.eventbooking.bookingservice.messaging;

public record EmailConfirmationMessage(
        String customerEmail,
        Long eventId,
        Integer seatsBooked
) {
}
