package com.eventbooking.eventservice.service;

import com.eventbooking.eventservice.dto.EventRequest;
import com.eventbooking.eventservice.dto.EventResponse;
import com.eventbooking.eventservice.entity.Event;
import com.eventbooking.eventservice.exception.EventNotFoundException;
import com.eventbooking.eventservice.exception.InsufficientSeatsException;
import com.eventbooking.eventservice.exception.InvalidTotalSeatsException;
import com.eventbooking.eventservice.mapper.EventMapper;
import com.eventbooking.eventservice.messaging.CatalogEventPublisher;
import com.eventbooking.eventservice.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CatalogEventPublisher catalogEventPublisher;

    public EventService(EventRepository eventRepository, CatalogEventPublisher catalogEventPublisher) {
        this.eventRepository = eventRepository;
        this.catalogEventPublisher = catalogEventPublisher;
    }

    public EventResponse createEvent(EventRequest request) {
        Event event = EventMapper.toEntity(request);
        event.setAvailableSeats(event.getTotalSeats()); // Business rule: available seats = total seats

        Event savedEvent = eventRepository.save(event);
        publishCatalogEvent(() -> catalogEventPublisher.publishCreated(savedEvent), savedEvent.getId());

        return EventMapper.toResponse(savedEvent);
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

        Event savedEvent = eventRepository.save(existingEvent);
        publishCatalogEvent(() -> catalogEventPublisher.publishUpdated(savedEvent), savedEvent.getId());

        return EventMapper.toResponse(savedEvent);
    }

    public void deleteEvent(Long id) {
        Event event = findEventOrThrow(id);
        publishCatalogEvent(() -> catalogEventPublisher.publishDeleted(event.getId()), event.getId());
        eventRepository.delete(event);
    }

    private void publishCatalogEvent(Runnable publishAction, Long eventId) {
        try {
            publishAction.run();
        } catch (Exception e) {
            log.error("Failed to publish catalog event for event {}: {}", eventId, e.getMessage(), e);
            // Known limitation: Search Service's read model will be stale until Phase 6's
            // outbox pattern replaces this best-effort publish.
        }
    }

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    public EventResponse reserveSeats(Long eventId, int seatsToReserve) {
        Event event = findEventOrThrow(eventId);

        if (event.getAvailableSeats() < seatsToReserve) {
            throw new InsufficientSeatsException(eventId, seatsToReserve, event.getAvailableSeats());
        }

        event.setAvailableSeats(event.getAvailableSeats() - seatsToReserve);
        Event updatedEvent = eventRepository.save(event);
        return EventMapper.toResponse(updatedEvent);
    }

    public EventResponse releaseSeats(Long eventId, int seatsToRelease) {
        Event event = findEventOrThrow(eventId);
        event.setAvailableSeats(event.getAvailableSeats() + seatsToRelease);
        Event updatedEvent = eventRepository.save(event);
        return EventMapper.toResponse(updatedEvent);
    }

    private Integer calculateUpdatedAvailableSeats(Event event, Integer newTotalSeats) {
        int soldPlaces = event.getTotalSeats() - event.getAvailableSeats();
        if (newTotalSeats < soldPlaces) {
            throw (new InvalidTotalSeatsException(newTotalSeats, soldPlaces));
        }
        return newTotalSeats - soldPlaces;
    }
}
