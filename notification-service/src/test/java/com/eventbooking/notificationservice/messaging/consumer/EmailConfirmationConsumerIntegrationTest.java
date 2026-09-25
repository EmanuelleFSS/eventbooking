package com.eventbooking.notificationservice.messaging.consumer;

import com.eventbooking.notificationservice.AbstractIntegrationTest;
import com.eventbooking.notificationservice.config.RabbitMQConfig;
import com.eventbooking.notificationservice.messaging.EmailConfirmationMessage;
import com.eventbooking.notificationservice.repository.ProcessedNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class EmailConfirmationConsumerIntegrationTest extends AbstractIntegrationTest {

    private static final String NOTIFICATION_TYPE = "EMAIL_CONFIRMATION";

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProcessedNotificationRepository processedNotificationRepository;

    @BeforeEach
    void cleanUp() {
        processedNotificationRepository.deleteAll();
    }

    @BeforeEach
    void purgeQueue() {
        rabbitAdmin.purgeQueue(RabbitMQConfig.EMAIL_QUEUE, false);
    }

    @Test
    void shouldProcessMessageOnlyOnce_whenPublishedTwice() {
        EmailConfirmationMessage message = new EmailConfirmationMessage(
                "test@example.com", 10L, 1L, 2);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, message);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(processedNotificationRepository
                        .existsByBookingIdAndNotificationType(1L, NOTIFICATION_TYPE))
                        .isTrue());

        long countAfterFirstMessage = processedNotificationRepository.count();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, message);

        await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(processedNotificationRepository.count()).isEqualTo(countAfterFirstMessage));
    }

    @Test
    void shouldConsumeMessage_whenPublishedWithBookingServiceStyleTypeHeader() {
        Long bookingId = 42L;
        String payload = """
                {"customerEmail":"cross-service@example.com","eventId":7,"bookingId":%d,"seatsBooked":3}
                """.formatted(bookingId);

        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setHeader("__TypeId__", "emailConfirmationMessage");
        Message message = new Message(payload.getBytes(StandardCharsets.UTF_8), properties);

        rabbitTemplate.send(RabbitMQConfig.EMAIL_QUEUE, message);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(processedNotificationRepository.existsByBookingIdAndNotificationType(
                        bookingId, "EMAIL_CONFIRMATION")).isTrue());
    }
}
