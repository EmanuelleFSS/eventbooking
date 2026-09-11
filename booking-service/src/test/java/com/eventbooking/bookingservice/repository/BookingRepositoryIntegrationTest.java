package com.eventbooking.bookingservice.repository;

import com.eventbooking.bookingservice.AbstractIntegrationTest;
import com.eventbooking.bookingservice.entity.Booking;
import com.eventbooking.bookingservice.entity.BookingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class BookingRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void shouldSaveAndRetrieveBooking() {
        Booking booking = new Booking();
        booking.setEventId(1L);
        booking.setCustomerEmail("test@example.com");
        booking.setSeatsBooked(2);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(Instant.now());

        Booking savedBooking = bookingRepository.save(booking);

        assertThat(savedBooking.getId()).isNotNull();

        Optional<Booking> retrieved = bookingRepository.findById(savedBooking.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getCustomerEmail()).isEqualTo("test@example.com");
    }
}
