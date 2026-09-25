package com.eventbooking.notificationservice.messaging.consumer;

import com.eventbooking.notificationservice.AbstractIntegrationTest;
import com.eventbooking.notificationservice.messaging.BookingEvent;
import com.eventbooking.notificationservice.repository.ProcessedNotificationRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(BookingEventConsumerIntegrationTest.TestProducerConfig.class)
public class BookingEventConsumerIntegrationTest extends AbstractIntegrationTest {

    private static final String TOPIC = "booking-events";

    @Autowired
    @Qualifier("testKafkaTemplate")
    private KafkaTemplate<String, Object> testKafkaTemplate;

    @Autowired
    private ProcessedNotificationRepository processedNotificationRepository;

    @BeforeEach
    void cleanUp() {
        processedNotificationRepository.deleteAll();
    }

    @Test
    void shouldProcessEventOnlyOnce_whenPublishedTwice() {
        BookingEvent event = new BookingEvent(
                5L, 20L, "test@example.com", 3, "CREATED", Instant.now());

        testKafkaTemplate.send(TOPIC, event.eventId().toString(), event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(processedNotificationRepository
                        .existsByBookingIdAndNotificationType(5L, "BOOKING_CREATED"))
                        .isTrue());

        long countAfterFirstMessage = processedNotificationRepository.count();

        testKafkaTemplate.send(TOPIC, event.eventId().toString(), event);

        await().pollDelay(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(processedNotificationRepository.count()).isEqualTo(countAfterFirstMessage));
    }

    @TestConfiguration
    static class TestProducerConfig {

        @Bean
        public ProducerFactory<String, Object> testProducerFactory() {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    BookingEventConsumerIntegrationTest.kafka.getBootstrapServers());
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(configProps);
        }

        @Bean
        public KafkaTemplate<String, Object> testKafkaTemplate(
                ProducerFactory<String, Object> testProducerFactory) {
            return new KafkaTemplate<>(testProducerFactory);
        }
    }
}
