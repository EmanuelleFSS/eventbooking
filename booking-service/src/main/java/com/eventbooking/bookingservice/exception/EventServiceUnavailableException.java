package com.eventbooking.bookingservice.exception;

public class EventServiceUnavailableException extends RuntimeException {
    public EventServiceUnavailableException(Long eventId, Throwable cause) {
        super("Event Service is unavailable while trying to reserve seats for event " + eventId, cause);
    }
}
