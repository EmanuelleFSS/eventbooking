package com.eventbooking.searchservice.messaging.consumer;

import com.eventbooking.searchservice.document.EventSearchDocument;
import com.eventbooking.searchservice.document.ProcessedBookingEventDocument;
import com.eventbooking.searchservice.messaging.BookingEvent;
import com.eventbooking.searchservice.repository.EventSearchRepository;
import com.eventbooking.searchservice.repository.ProcessedBookingEventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    private final EventSearchRepository repository;
    private final ProcessedBookingEventRepository processedBookingEventRepository;

    public BookingEventConsumer(EventSearchRepository repository, ProcessedBookingEventRepository processedBookingEventRepository) {
        this.repository = repository;
        this.processedBookingEventRepository = processedBookingEventRepository;
    }

    @KafkaListener(topics = "booking-events", groupId = "search-service")
    public void handleBookingEvent(BookingEvent event) {
        String dedupeKey = event.bookingId() + "-" + event.status(); // ex: 5-CREATED

        if (processedBookingEventRepository.existsById(dedupeKey)) {
            return;
        }

        EventSearchDocument document = repository.findById(event.eventId().toString())
                .orElseGet(() -> {
                    EventSearchDocument placeholder = new EventSearchDocument();
                    placeholder.setId(event.eventId().toString());
                    placeholder.setBookingsCount(0);
                    return placeholder;
                });

        int delta = "CREATED".equals(event.status()) ? 1 : -1;
        document.setBookingsCount(Math.max(0, document.getBookingsCount() + delta));
        repository.save(document);

        ProcessedBookingEventDocument processed = new ProcessedBookingEventDocument();
        processed.setId(dedupeKey);
        processedBookingEventRepository.save(processed);
    }
}
