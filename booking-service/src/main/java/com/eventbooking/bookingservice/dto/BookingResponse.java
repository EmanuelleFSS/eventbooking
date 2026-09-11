package com.eventbooking.bookingservice.dto;

import com.eventbooking.bookingservice.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long eventId;
    private String customerEmail;
    private Integer seatsBooked;
    private BookingStatus status;
    private Instant createdAt;
}