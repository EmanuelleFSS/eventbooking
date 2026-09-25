package com.eventbooking.notificationservice.messaging.consumer;

import com.eventbooking.notificationservice.entity.ProcessedNotification;
import com.eventbooking.notificationservice.messaging.EmailConfirmationMessage;
import com.eventbooking.notificationservice.repository.ProcessedNotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class EmailConfirmationConsumer {

    private static final String NOTIFICATION_TYPE = "EMAIL_CONFIRMATION";

    private final ProcessedNotificationRepository processedNotificationRepository;

    public EmailConfirmationConsumer(ProcessedNotificationRepository processedNotificationRepository) {
        this.processedNotificationRepository = processedNotificationRepository;
    }

    @RabbitListener(queues = "booking.email-confirmation")
    public void handleEmailConfirmation(EmailConfirmationMessage message) {
        ProcessedNotification record = new ProcessedNotification();
        record.setBookingId(message.bookingId());
        record.setNotificationType(NOTIFICATION_TYPE);
        record.setProcessedAt(Instant.now());

        try {
            processedNotificationRepository.save(record);
        } catch (DataIntegrityViolationException e) {
            return;
        }

        System.out.println("Sending confirmation email to " + message.customerEmail()
                + " for event " + message.eventId());
    }
}
