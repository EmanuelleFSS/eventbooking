package com.eventbooking.eventservice.repository;

import com.eventbooking.eventservice.AbstractIntegrationTest;
import com.eventbooking.eventservice.entity.Event;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class EventRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveEvent() {
        Event event = new Event();
        event.setTitle("Jazz Concert");
        event.setEventDate(OffsetDateTime.now().plusDays(10));
        event.setLocation("Paris");
        event.setTotalSeats(100);
        event.setAvailableSeats(100);
        event.setCreatedAt(Instant.now());

        Event savedEvent = eventRepository.save(event);

        assertThat(savedEvent.getId()).isNotNull();

        Optional<Event> retrievedEvent = eventRepository.findById(savedEvent.getId());

        assertThat(retrievedEvent).isPresent();
        assertThat(retrievedEvent.get().getTitle()).isEqualTo("Jazz Concert");
    }

    @Test
    void shouldThrowOptimisticLockingException_whenTwoTransactionsUpdateSameEventConcurrently() {
        Event event = new Event();
        event.setTitle("Jazz Concert");
        event.setEventDate(OffsetDateTime.now().plusDays(10));
        event.setLocation("Paris");
        event.setTotalSeats(100);
        event.setAvailableSeats(100);
        event.setCreatedAt(Instant.now());

        Event savedEvent = eventRepository.saveAndFlush(event);
        Long eventId = savedEvent.getId();

        entityManager.clear();

        Event firstCopy = eventRepository.findById(eventId).orElseThrow();
        entityManager.detach(firstCopy);

        Event secondCopy = eventRepository.findById(eventId).orElseThrow();
        entityManager.detach(secondCopy);

        firstCopy.setTitle("Updated by first session");
        eventRepository.saveAndFlush(firstCopy);

        secondCopy.setTitle("Updated by second session");
        assertThatThrownBy(() -> eventRepository.saveAndFlush(secondCopy))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }
}
