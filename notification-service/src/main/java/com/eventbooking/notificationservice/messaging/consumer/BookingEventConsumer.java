package com.eventbooking.notificationservice.messaging.consumer;

import com.eventbooking.notificationservice.entity.ProcessedNotification;
import com.eventbooking.notificationservice.messaging.BookingEvent;
import com.eventbooking.notificationservice.repository.ProcessedNotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BookingEventConsumer {

    private final ProcessedNotificationRepository processedNotificationRepository;

    public BookingEventConsumer(ProcessedNotificationRepository processedNotificationRepository) {
        this.processedNotificationRepository = processedNotificationRepository;
    }

    @KafkaListener(topics = "booking-events", groupId = "notification-service")
    public void handleBookingEvent(BookingEvent event) {
        String notificationType = "BOOKING_" + event.status();

        if (processedNotificationRepository.existsByBookingIdAndNotificationType(event.bookingId(), notificationType)) {
            return;
        }

        System.out.println("Notification: booking " + event.bookingId() + " is now " + event.status());

        ProcessedNotification record = new ProcessedNotification();
        record.setBookingId(event.bookingId());
        record.setNotificationType(notificationType);
        record.setProcessedAt(Instant.now());
        processedNotificationRepository.save(record);
    }
}
