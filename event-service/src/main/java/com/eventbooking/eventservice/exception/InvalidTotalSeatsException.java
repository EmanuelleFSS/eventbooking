package com.eventbooking.eventservice.exception;

public class InvalidTotalSeatsException extends RuntimeException {

    public InvalidTotalSeatsException(Integer requestedTotalSeats, int soldSeats) {
        super("Cannot set total seats to " + requestedTotalSeats +
                ": " + soldSeats + " seats have already been sold.");
    }
}
