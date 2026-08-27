package com.eventbooking.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private Integer totalSeats;
    private Integer availableSeats;
    private LocalDateTime createdAt;
}
