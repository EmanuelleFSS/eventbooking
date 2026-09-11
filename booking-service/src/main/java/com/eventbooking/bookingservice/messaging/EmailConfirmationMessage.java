package com.eventbooking.bookingservice.messaging;

import java.io.Serializable;

public record EmailConfirmationMessage (
        String customerEmail,
        Long eventId,
        Integer seatsBooked
) implements Serializable {
}
