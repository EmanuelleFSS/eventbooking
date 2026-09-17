package com.eventbooking.searchservice.messaging.consumer;

import com.eventbooking.searchservice.document.EventSearchDocument;
import com.eventbooking.searchservice.messaging.CatalogEvent;
import com.eventbooking.searchservice.repository.EventSearchRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventCatalogConsumer {

    private final EventSearchRepository repository;

    public EventCatalogConsumer(EventSearchRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "event-catalog-events", groupId = "search-service")
    public void handleCatalogEvent(CatalogEvent event) {
        switch (event.changeType()) {
            case "CREATED", "UPDATED" -> upsertDocument(event);
            case "DELETED" -> repository.deleteById(event.eventId().toString());
            default -> {}
        }
    }

    private void upsertDocument(CatalogEvent event) {
        EventSearchDocument document = repository.findById(event.eventId().toString())
                .orElseGet(EventSearchDocument::new);

        document.setId(event.eventId().toString());
        document.setTitle(event.title());
        document.setLocation(event.location());
        document.setEventDate(event.eventDate());

        repository.save(document);
    }
}
