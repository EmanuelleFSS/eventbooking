package com.eventbooking.eventservice.repository;

import com.eventbooking.eventservice.AbstractIntegrationTest;
import com.eventbooking.eventservice.entity.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EventRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldSaveAndRetrieveEvent() {
        Event event = new Event();
        event.setTitle("Jazz Concert");
        event.setEventDate(LocalDateTime.now().plusDays(10));
        event.setLocation("Paris");
        event.setTotalSeats(100);
        event.setAvailableSeats(100);
        event.setCreatedAt(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);

        assertThat(savedEvent.getId()).isNotNull();

        Optional<Event> retrievedEvent = eventRepository.findById(savedEvent.getId());

        assertThat(retrievedEvent).isPresent();
        assertThat(retrievedEvent.get().getTitle()).isEqualTo("Jazz Concert");
    }
}
