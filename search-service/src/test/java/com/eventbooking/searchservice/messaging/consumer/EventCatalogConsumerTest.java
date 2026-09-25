package com.eventbooking.searchservice.messaging.consumer;

import com.eventbooking.searchservice.document.EventSearchDocument;
import com.eventbooking.searchservice.messaging.CatalogEvent;
import com.eventbooking.searchservice.repository.EventSearchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventCatalogConsumerTest {

    @Mock
    private EventSearchRepository repository;

    @InjectMocks
    private EventCatalogConsumer consumer;

    @Test
    void handleCatalogEvent_shouldCreateDocument_whenEventDoesNotExistYet() {
        when(repository.findById("1")).thenReturn(Optional.empty());

        CatalogEvent event = new CatalogEvent(
                1L, "Jazz Concert", "Paris",
                OffsetDateTime.now().plusDays(10), "CREATED", Instant.now());

        consumer.handleCatalogEvent(event);

        ArgumentCaptor<EventSearchDocument> captor = ArgumentCaptor.forClass(EventSearchDocument.class);
        verify(repository, times(1)).save(captor.capture());

        EventSearchDocument saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo("1");
        assertThat(saved.getTitle()).isEqualTo("Jazz Concert");
        assertThat(saved.getLocation()).isEqualTo("Paris");
        assertThat(saved.getBookingsCount()).isZero();
    }

    @Test
    void handleCatalogEvent_shouldUpdateDocument_withoutOverwritingBookingsCount() {
        EventSearchDocument existingDocument = new EventSearchDocument();
        existingDocument.setId("1");
        existingDocument.setTitle("Old Title");
        existingDocument.setLocation("Lyon");
        existingDocument.setBookingsCount(30);

        when(repository.findById("1")).thenReturn(Optional.of(existingDocument));

        CatalogEvent event = new CatalogEvent(
                1L, "Updated Title", "Paris",
                OffsetDateTime.now().plusDays(5), "UPDATED", Instant.now());

        consumer.handleCatalogEvent(event);

        ArgumentCaptor<EventSearchDocument> captor = ArgumentCaptor.forClass(EventSearchDocument.class);
        verify(repository, times(1)).save(captor.capture());

        EventSearchDocument saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Updated Title");
        assertThat(saved.getLocation()).isEqualTo("Paris");
        assertThat(saved.getBookingsCount()).isEqualTo(30); // préservé, pas écrasé
    }

    @Test
    void handleCatalogEvent_shouldDeleteDocument_whenChangeTypeIsDeleted() {
        CatalogEvent event = new CatalogEvent(
                1L, null, null, null, "DELETED", Instant.now());

        consumer.handleCatalogEvent(event);

        verify(repository, times(1)).deleteById("1");
        verify(repository, never()).save(any(EventSearchDocument.class));
    }
}
