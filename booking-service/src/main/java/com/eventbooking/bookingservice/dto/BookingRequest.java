package com.eventbooking.bookingservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "Event id is required")
    private Long eventId;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    private String customerEmail;

    @NotNull(message = "Number of seats is required")
    @Positive(message = "Number of seats must be positive")
    private Integer seatsBooked;
}
