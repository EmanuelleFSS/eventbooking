package com.eventbooking.eventservice.service;

import com.eventbooking.eventservice.dto.EventRequest;
import com.eventbooking.eventservice.dto.EventResponse;
import com.eventbooking.eventservice.entity.Event;
import com.eventbooking.eventservice.exception.EventNotFoundException;
import com.eventbooking.eventservice.exception.InvalidTotalSeatsException;
import com.eventbooking.eventservice.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private Event existingEvent;

    @BeforeEach
    void setUp() {
        existingEvent = new Event();
        existingEvent.setId(1L);
        existingEvent.setTitle("Jazz Concert");
        existingEvent.setEventDate(LocalDateTime.now().plusDays(10));
        existingEvent.setLocation("Paris");
        existingEvent.setTotalSeats(100);
        existingEvent.setAvailableSeats(70);
        existingEvent.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void creatEvent_shouldSetAvailableSeatsEqualToTotalSeats() {
        EventRequest request = new EventRequest();
        request.setTitle("New Event");
        request.setEventDate(LocalDateTime.now().plusDays(5));
        request.setLocation("Lyon");
        request.setTotalSeats(50);

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event savedEvent = invocation.getArgument(0);
            savedEvent.setId(2L);
            return savedEvent;
        });

        EventResponse response = eventService.createEvent(request);

        assertThat(response.getAvailableSeats()).isEqualTo(50);
        assertThat(response.getTotalSeats()).isEqualTo(50);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void getEventById_shouldReturnEvent_whenEventExists() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

        EventResponse response = eventService.getEventById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Jazz Concert");
    }

    @Test
    void getEventById_shouldThrowException_whenEventDoesNotExist() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(999L))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deleteEvent_shouldCallRepositoryDelete_whenEventExists() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

        eventService.deleteEvent(1L);

        verify(eventRepository, times(1)).delete(existingEvent);
    }

    @Test
    void updateEvent_shouldRecalculateAvailableSeats_whenNewTotalSeatsIsValid() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventRequest request = new EventRequest();
        request.setTitle("Jazz Concert Updated");
        request.setEventDate(existingEvent.getEventDate());
        request.setLocation("Paris");
        request.setTotalSeats(80); // 30 already sold, so 50 should remain available

        EventResponse response = eventService.updateEvent(1L, request);

        assertThat(response.getTotalSeats()).isEqualTo(80);
        assertThat(response.getAvailableSeats()).isEqualTo(50); // 80 - 30 sold
    }

    @Test
    void updateEvent_shouldThrowException_whenNewTotalSeatsIsLowerThanSoldSeats() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

        EventRequest request = new EventRequest();
        request.setTitle("Jazz Concert Updated");
        request.setEventDate(existingEvent.getEventDate());
        request.setLocation("Paris");
        request.setTotalSeats(20); // lower than the 30 already sold — must be rejected

        assertThatThrownBy(() -> eventService.updateEvent(1L, request))
                .isInstanceOf(InvalidTotalSeatsException.class);

        verify(eventRepository, never()).save(any(Event.class));
    }
}
