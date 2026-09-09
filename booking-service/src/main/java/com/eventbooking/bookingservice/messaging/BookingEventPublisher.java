package com.eventbooking.bookingservice.messaging;

import com.eventbooking.bookingservice.entity.Booking;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BookingEventPublisher {

    private static final String TOPIC = "booking-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BookingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBookingCreated(Booking booking) {
        BookingEvent event = new BookingEvent(
                booking.getId(),
                booking.getEventId(),
                booking.getCustomerEmail(),
                booking.getSeatsBooked(),
                "CREATED",
                Instant.now()
        );
        kafkaTemplate.send(TOPIC, booking.getEventId().toString(), event);
    }

    public void publishBookingCancelled(Booking booking) {
        BookingEvent event = new BookingEvent(
                booking.getId(),
                booking.getEventId(),
                booking.getCustomerEmail(),
                booking.getSeatsBooked(),
                "CANCELLED",
                Instant.now()
        );
        kafkaTemplate.send(TOPIC, booking.getEventId().toString(), event);
    }
}
