package com.eventbooking.searchservice.messaging.consumer;

import com.eventbooking.searchservice.document.EventSearchDocument;
import com.eventbooking.searchservice.messaging.BookingEvent;
import com.eventbooking.searchservice.repository.EventSearchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingEventConsumerTest {

    @Mock
    private EventSearchRepository repository;

    @InjectMocks
    private BookingEventConsumer consumer;

    @Test
    void handleBookingEvent_shouldIncrementBookingsCount_whenStatusIsCreated() {
        EventSearchDocument document = new EventSearchDocument();
        document.setId("1");
        document.setBookingsCount(10);

        when(repository.findById("1")).thenReturn(Optional.of(document));

        BookingEvent event = new BookingEvent(
                100L, 1L, "test@example.com", 2, "CREATED", Instant.now());

        consumer.handleBookingEvent(event);

        assertThat(document.getBookingsCount()).isEqualTo(11);
        verify(repository, times(1)).save(document);
    }

    @Test
    void handleBookingEvent_shouldDecrementBookingsCount_whenStatusIsCancelled() {
        EventSearchDocument document = new EventSearchDocument();
        document.setId("1");
        document.setBookingsCount(10);

        when(repository.findById("1")).thenReturn(Optional.of(document));

        BookingEvent event = new BookingEvent(
                100L, 1L, "test@example.com", 2, "CANCELLED", Instant.now());

        consumer.handleBookingEvent(event);

        assertThat(document.getBookingsCount()).isEqualTo(9);
        verify(repository, times(1)).save(document);
    }

    @Test
    void handleBookingEvent_shouldNotThrow_whenEventIdDoesNotExist() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        BookingEvent event = new BookingEvent(
                100L, 999L, "test@example.com", 2, "CREATED", Instant.now());

        consumer.handleBookingEvent(event);

        verify(repository, never()).save(any(EventSearchDocument.class));
    }
}
