package com.eventbooking.bookingservice.exception;

import java.time.Instant;
import java.util.Map;

public record ValidationErrorResponse (
        int status,
        String message,
        Instant timestamp,
        Map<String, String> fieldErrors
){
}
