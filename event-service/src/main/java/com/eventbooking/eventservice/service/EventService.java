package com.eventbooking.eventservice.service;

import com.eventbooking.eventservice.dto.EventRequest;
import com.eventbooking.eventservice.dto.EventResponse;
import com.eventbooking.eventservice.entity.Event;
import com.eventbooking.eventservice.exception.EventNotFoundException;
import com.eventbooking.eventservice.exception.InvalidTotalSeatsException;
import com.eventbooking.eventservice.mapper.EventMapper;
import com.eventbooking.eventservice.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventResponse createEvent(EventRequest request) {
        Event event = EventMapper.toEntity(request);
        event.setAvailableSeats(event.getTotalSeats()); // Business rule: available seats = total seats
        return EventMapper.toResponse(eventRepository.save(event));
    }

    public EventResponse getEventById(Long id) {
        Event event = findEventOrThrow(id);
        return EventMapper.toResponse(event);
    }

    public Page<EventResponse> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(EventMapper::toResponse);
    }

    public EventResponse updateEvent(Long id, EventRequest request) {
        Event existingEvent = findEventOrThrow(id);
        int updatedAvailableSeats = calculateUpdatedAvailableSeats(existingEvent, request.getTotalSeats());

        existingEvent.setTitle(request.getTitle());
        existingEvent.setDescription(request.getDescription());
        existingEvent.setEventDate(request.getEventDate());
        existingEvent.setLocation(request.getLocation());
        existingEvent.setTotalSeats(request.getTotalSeats());
        existingEvent.setAvailableSeats(updatedAvailableSeats);

        return EventMapper.toResponse(eventRepository.save(existingEvent));
    }

    public void deleteEvent(Long id) {
        Event event = findEventOrThrow(id);
        eventRepository.delete(event);
    }

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    private Integer calculateUpdatedAvailableSeats(Event event, Integer newTotalSeats) {
        int soldPlaces = event.getTotalSeats() - event.getAvailableSeats();
        if (newTotalSeats < soldPlaces) {
            throw (new InvalidTotalSeatsException(newTotalSeats, soldPlaces));
        }
        return newTotalSeats - soldPlaces;
    }
}
