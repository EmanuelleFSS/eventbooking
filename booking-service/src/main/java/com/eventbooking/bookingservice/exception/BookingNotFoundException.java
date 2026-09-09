package com.eventbooking.bookingservice.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(Long BookingId) {
        super("Booking not found with id: " + BookingId);
    }
}
