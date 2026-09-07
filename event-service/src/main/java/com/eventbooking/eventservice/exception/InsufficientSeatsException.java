package com.eventbooking.eventservice.exception;

public class InsufficientSeatsException extends RuntimeException {

    public InsufficientSeatsException(Long eventId, int requestedSeats, int availableSeats) {
        super("Cannot reserve " + requestedSeats + " seat(s) for event " + eventId +
                ": only " + availableSeats + " seat(s) available.");
    }
}
