package com.eventbooking.notificationservice.messaging.consumer;

import com.eventbooking.notificationservice.AbstractIntegrationTest;
import com.eventbooking.notificationservice.messaging.EmailConfirmationMessage;
import com.eventbooking.notificationservice.messaging.config.RabbitMQConfig;
import com.eventbooking.notificationservice.repository.ProcessedNotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class EmailConfirmationConsumerIntegrationTest extends AbstractIntegrationTest {

    private static final String NOTIFICATION_TYPE = "EMAIL_CONFIRMATION";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProcessedNotificationRepository processedNotificationRepository;

    @Test
    void shouldProcessMessageOnlyOnce_whenPublishedTwice() {
        EmailConfirmationMessage message = new EmailConfirmationMessage(
                "test@example.com", 10L, 1L, 2);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, message);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(processedNotificationRepository
                        .existsByBookingIdAndNotificationType(1L, NOTIFICATION_TYPE))
                        .isTrue());

        long countAfterFirstMessage = processedNotificationRepository.count();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, message);

        await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(processedNotificationRepository.count()).isEqualTo(countAfterFirstMessage));
    }
}
