package com.eventbooking.searchservice.messaging.consumer;

import com.eventbooking.searchservice.messaging.BookingEvent;
import com.eventbooking.searchservice.repository.EventSearchRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    private final EventSearchRepository repository;

    public BookingEventConsumer(EventSearchRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "booking-events", groupId = "search-service")
    public void handleBookingEvent(BookingEvent event) {
        repository.findById(event.eventId().toString()).ifPresent(document -> {
            int delta = "CREATED".equals(event.status()) ? 1 : -1;
            document.setBookingsCount(Math.max(0, document.getBookingsCount() + delta));
            repository.save(document);
        });
    }
}
