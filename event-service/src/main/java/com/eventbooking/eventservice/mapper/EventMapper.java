package com.eventbooking.eventservice.mapper;

import com.eventbooking.eventservice.dto.EventRequest;
import com.eventbooking.eventservice.dto.EventResponse;
import com.eventbooking.eventservice.entity.Event;

import java.time.Instant;

public final class EventMapper {

    private EventMapper() {}

    public static Event toEntity(EventRequest request) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setLocation(request.getLocation());
        event.setTotalSeats(request.getTotalSeats());
        event.setCreatedAt(Instant.now());
        return event;
    }

    public static EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .totalSeats(event.getTotalSeats())
                .availableSeats(event.getAvailableSeats())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
