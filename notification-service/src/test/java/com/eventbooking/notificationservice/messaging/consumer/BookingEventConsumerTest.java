package com.eventbooking.notificationservice.messaging.consumer;

import com.eventbooking.notificationservice.entity.ProcessedNotification;
import com.eventbooking.notificationservice.messaging.BookingEvent;
import com.eventbooking.notificationservice.repository.ProcessedNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingEventConsumerTest {

    @Mock
    private ProcessedNotificationRepository processedNotificationRepository;

    @InjectMocks
    private BookingEventConsumer bookingEventConsumer;

    @Test
    void handleBookingEvent_shouldSaveProcessedNotification_forCreatedStatus() {
        BookingEvent event = new BookingEvent(
                5L, 20L, "test@example.com", 3, "CREATED", Instant.now());

        when(processedNotificationRepository.existsByBookingIdAndNotificationType(5L, "BOOKING_CREATED"))
                .thenReturn(false);

        bookingEventConsumer.handleBookingEvent(event);

        ArgumentCaptor<ProcessedNotification> captor = ArgumentCaptor.forClass(ProcessedNotification.class);
        verify(processedNotificationRepository).save(captor.capture());

        ProcessedNotification saved = captor.getValue();
        assertThat(saved.getBookingId()).isEqualTo(5L);
        assertThat(saved.getNotificationType()).isEqualTo("BOOKING_CREATED");
    }

    @Test
    void handleBookingEvent_shouldSaveProcessedNotification_forCancelledStatus() {
        BookingEvent event = new BookingEvent(
                5L, 20L, "test@example.com", 3, "CANCELLED", Instant.now());

        when(processedNotificationRepository.existsByBookingIdAndNotificationType(5L, "BOOKING_CANCELLED"))
                .thenReturn(false);

        bookingEventConsumer.handleBookingEvent(event);

        ArgumentCaptor<ProcessedNotification> captor = ArgumentCaptor.forClass(ProcessedNotification.class);
        verify(processedNotificationRepository).save(captor.capture());

        assertThat(captor.getValue().getNotificationType()).isEqualTo("BOOKING_CANCELLED");
    }

    @Test
    void handleBookingEvent_shouldDoNothing_whenAlreadyProcessed() {
        BookingEvent event = new BookingEvent(
                5L, 20L, "test@example.com", 3, "CREATED", Instant.now());

        when(processedNotificationRepository.existsByBookingIdAndNotificationType(5L, "BOOKING_CREATED"))
                .thenReturn(true);

        bookingEventConsumer.handleBookingEvent(event);

        verify(processedNotificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
