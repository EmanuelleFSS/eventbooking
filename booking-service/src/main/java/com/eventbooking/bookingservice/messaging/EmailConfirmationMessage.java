package com.eventbooking.bookingservice.messaging;

import java.io.Serializable;

public record EmailConfirmationMessage (
        String customerEmail,
        Long eventId,
        Long bookingId,
        Integer seatsBooked
) implements Serializable {
}
