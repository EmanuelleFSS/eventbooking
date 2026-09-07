package com.eventbooking.eventservice.controller;

import com.eventbooking.eventservice.dto.EventRequest;
import com.eventbooking.eventservice.dto.EventResponse;
import com.eventbooking.eventservice.dto.SeatsRequest;
import com.eventbooking.eventservice.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public  EventController(EventService eventService){
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<EventResponse>> getAllEvents(Pageable pageable) {
        Page<EventResponse> responses = eventService.getAllEvents(pageable);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id,
                                                     @Valid @RequestBody EventRequest request) {
        EventResponse eventResponse = eventService.updateEvent(id, request);
        return ResponseEntity.ok(eventResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reserve-seats")
    public ResponseEntity<EventResponse> reserveSeats(@PathVariable Long id, @Valid @RequestBody SeatsRequest request) {
        EventResponse eventResponse = eventService.reserveSeats(id,request.getSeats());
        return ResponseEntity.ok(eventResponse);
    }

    @PatchMapping("/{id}/release-seats")
    public ResponseEntity<EventResponse> releaseSeats(@PathVariable Long id, @Valid @RequestBody SeatsRequest request) {
        EventResponse eventResponse = eventService.releaseSeats(id, request.getSeats());
        return ResponseEntity.ok(eventResponse);
    }
}
