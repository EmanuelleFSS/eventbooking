package com.eventbooking.eventservice.messaging;

import com.eventbooking.eventservice.entity.Event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CatalogEventPublisher {

    private static final String TOPIC = "event-catalog-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CatalogEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCreated(Event event) {
        publish(event, "CREATED");
    }

    public void publishUpdated(Event event) {
        publish(event, "UPDATED");
    }

    public void publishDeleted(Long eventId) {
        CatalogEvent catalogEvent = new CatalogEvent(
                eventId, null, null, null, "DELETED", Instant.now());
        kafkaTemplate.send(TOPIC, eventId.toString(), catalogEvent);
    }

    private void publish(Event event, String changeType) {
        CatalogEvent catalogEvent = new CatalogEvent(
                event.getId(),
                event.getTitle(),
                event.getLocation(),
                event.getEventDate(),
                changeType,
                Instant.now()
        );
        kafkaTemplate.send(TOPIC, event.getId().toString(), catalogEvent);
    }
}
