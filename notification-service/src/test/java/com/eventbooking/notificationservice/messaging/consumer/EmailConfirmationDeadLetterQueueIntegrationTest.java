package com.eventbooking.notificationservice.messaging.consumer;

import com.eventbooking.notificationservice.AbstractIntegrationTest;
import com.eventbooking.notificationservice.config.RabbitMQConfig;
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
public class EmailConfirmationDeadLetterQueueIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void purgeDeadLetterQueue() {
        rabbitAdmin.purgeQueue(RabbitMQConfig.EMAIL_DLQ, false);
    }

    @Test
    void shouldRouteToDeadLetterQueue_whenMessageFailsRepeatedly() {
        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setHeader("__TypeId__", "emailConfirmationMessage");
        Message malformedMessage = new Message("{not-valid-json".getBytes(StandardCharsets.UTF_8), properties);

        rabbitTemplate.send(RabbitMQConfig.EMAIL_QUEUE, malformedMessage);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Message deadLettered = rabbitTemplate.receive(RabbitMQConfig.EMAIL_DLQ);
            assertThat(deadLettered).isNotNull();
        });
    }
}
