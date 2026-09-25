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
import org.springframework.dao.DataIntegrityViolationException;

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

        bookingEventConsumer.handleBookingEvent(event);

        ArgumentCaptor<ProcessedNotification> captor = ArgumentCaptor.forClass(ProcessedNotification.class);
        verify(processedNotificationRepository).save(captor.capture());

        assertThat(captor.getValue().getNotificationType()).isEqualTo("BOOKING_CANCELLED");
    }

    @Test
    void handleBookingEvent_shouldNotThrow_whenInsertFailsWithUniqueConstraintViolation() {
        BookingEvent event = new BookingEvent(
                5L, 20L, "test@example.com", 3, "CREATED", Instant.now());

        doThrow(new DataIntegrityViolationException("duplicate key"))
                .when(processedNotificationRepository).save(any(ProcessedNotification.class));

        bookingEventConsumer.handleBookingEvent(event);

        verify(processedNotificationRepository, times(1)).save(any(ProcessedNotification.class));
    }
}
