package com.eventbooking.bookingservice.exception;

public class SeatsUnavailableException extends RuntimeException {
    public SeatsUnavailableException(Long eventId, int requestedSeats) {
        super("Not enough seats available for event " + eventId + " (" + requestedSeats + " requested).");
    }
}
