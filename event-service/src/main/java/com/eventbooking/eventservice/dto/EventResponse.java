package com.eventbooking.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private OffsetDateTime eventDate;
    private String location;
    private Integer totalSeats;
    private Integer availableSeats;
    private Instant createdAt;
}
