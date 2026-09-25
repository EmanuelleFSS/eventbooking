package com.eventbooking.notificationservice.messaging;

import java.io.Serializable;

public record EmailConfirmationMessage (
        String customerEmail,
        Long eventId,
        Long bookingId,
        Integer seatsBooked
) implements Serializable {
}
