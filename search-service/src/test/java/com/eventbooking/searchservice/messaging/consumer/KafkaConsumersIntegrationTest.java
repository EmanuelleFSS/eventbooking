package com.eventbooking.searchservice.messaging.consumer;

import com.eventbooking.searchservice.AbstractIntegrationTest;
import com.eventbooking.searchservice.document.EventSearchDocument;
import com.eventbooking.searchservice.messaging.BookingEvent;
import com.eventbooking.searchservice.messaging.CatalogEvent;
import com.eventbooking.searchservice.repository.EventSearchRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(KafkaConsumersIntegrationTest.TestProducerConfig.class)
class KafkaConsumersIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> testKafkaTemplate;

    @Autowired
    private EventSearchRepository repository;

    @Test
    void shouldConsumeBothCatalogAndBookingEvents_whenPublishedToTheirRespectiveTopics() {
        Long eventId = 100L;

        CatalogEvent catalogEvent = new CatalogEvent(
                eventId, "Test Concert", "Paris",
                OffsetDateTime.now().plusDays(10), "CREATED", Instant.now());
        testKafkaTemplate.send("event-catalog-events", eventId.toString(), catalogEvent);

        BookingEvent bookingEvent = new BookingEvent(
                1L, eventId, "test@example.com", 2, "CREATED", Instant.now());
        testKafkaTemplate.send("booking-events", eventId.toString(), bookingEvent);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(repository.findById(eventId.toString()))
                        .hasValueSatisfying(document -> {
                            assertThat(document.getTitle()).isEqualTo("Test Concert");
                            assertThat(document.getBookingsCount()).isEqualTo(1);
                        }));
    }

    @Test
    void shouldApplyBookingCount_whenBookingEventArrivesBeforeCatalogEvent() {
        Long eventId = 200L;

        BookingEvent bookingEvent = new BookingEvent(
                2L, eventId, "test2@example.com", 1, "CREATED", Instant.now());
        testKafkaTemplate.send("booking-events", eventId.toString(), bookingEvent);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(repository.findById(eventId.toString()))
                        .hasValueSatisfying(document -> assertThat(document.getBookingsCount()).isEqualTo(1)));

        CatalogEvent catalogEvent = new CatalogEvent(
                eventId, "Late Catalog Event", "Lyon",
                OffsetDateTime.now().plusDays(5), "CREATED", Instant.now());
        testKafkaTemplate.send("event-catalog-events", eventId.toString(), catalogEvent);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(repository.findById(eventId.toString()))
                        .hasValueSatisfying(document -> {
                            assertThat(document.getTitle()).isEqualTo("Late Catalog Event");
                            assertThat(document.getBookingsCount()).isEqualTo(1);
                        }));
    }

    @Test
    void shouldIncrementBookingsCountOnlyOnce_whenSameEventPublishedTwice() {
        Long eventId = 300L;

        EventSearchDocument seed = new EventSearchDocument();
        seed.setId(eventId.toString());
        seed.setTitle("Pre-existing Event");
        seed.setBookingsCount(0);
        repository.save(seed);

        BookingEvent event = new BookingEvent(
                99L, eventId, "duplicate@example.com", 1, "CREATED", Instant.now());

        testKafkaTemplate.send("booking-events", eventId.toString(), event);
        testKafkaTemplate.send("booking-events", eventId.toString(), event); // exact duplicate delivery

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(repository.findById(eventId.toString()))
                        .hasValueSatisfying(document -> assertThat(document.getBookingsCount()).isEqualTo(1)));
    }

    @TestConfiguration
    static class TestProducerConfig {

        @Bean
        public ProducerFactory<String, Object> testProducerFactory() {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    KafkaConsumersIntegrationTest.kafka.getBootstrapServers());
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
            configProps.put("spring.json.type.mapping",
                    "catalogEvent:com.eventbooking.searchservice.messaging.CatalogEvent,"
                            + "bookingEvent:com.eventbooking.searchservice.messaging.BookingEvent");
            return new DefaultKafkaProducerFactory<>(configProps);
        }

        @Bean
        public KafkaTemplate<String, Object> testKafkaTemplate(ProducerFactory<String, Object> testProducerFactory) {
            return new KafkaTemplate<>(testProducerFactory);
        }
    }
}