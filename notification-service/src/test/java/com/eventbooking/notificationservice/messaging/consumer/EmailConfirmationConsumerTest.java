package com.eventbooking.notificationservice.messaging.consumer;

import com.eventbooking.notificationservice.entity.ProcessedNotification;
import com.eventbooking.notificationservice.messaging.EmailConfirmationMessage;
import com.eventbooking.notificationservice.repository.ProcessedNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailConfirmationConsumerTest {

    private static final String NOTIFICATION_TYPE = "EMAIL_CONFIRMATION";

    @Mock
    private ProcessedNotificationRepository processedNotificationRepository;

    @InjectMocks
    private EmailConfirmationConsumer emailConfirmationConsumer;

    @Test
    void handleEmailConfirmation_shouldSaveProcessedNotification_whenNotAlreadyProcessed() {
        EmailConfirmationMessage message = new EmailConfirmationMessage(
                "test@example.com", 10L, 1L, 2);

        when(processedNotificationRepository.existsByBookingIdAndNotificationType(1L, NOTIFICATION_TYPE))
                .thenReturn(false);

        emailConfirmationConsumer.handleEmailConfirmation(message);

        ArgumentCaptor<ProcessedNotification> captor = ArgumentCaptor.forClass(ProcessedNotification.class);
        verify(processedNotificationRepository).save(captor.capture());

        ProcessedNotification saved = captor.getValue();
        assertThat(saved.getBookingId()).isEqualTo(1L);
        assertThat(saved.getNotificationType()).isEqualTo(NOTIFICATION_TYPE);
    }

    @Test
    void handleEmailConfirmation_shouldDoNothing_whenAlreadyProcessed() {
        EmailConfirmationMessage message = new EmailConfirmationMessage(
                "test@example.com", 10L, 1L, 2);

        when(processedNotificationRepository.existsByBookingIdAndNotificationType(1L, NOTIFICATION_TYPE))
                .thenReturn(true);

        emailConfirmationConsumer.handleEmailConfirmation(message);

        verify(processedNotificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
