package com.eventbooking.bookingservice.controller;

import com.eventbooking.bookingservice.AbstractIntegrationTest;
import com.eventbooking.bookingservice.client.EventServiceClient;
import com.eventbooking.bookingservice.dto.BookingRequest;
import com.eventbooking.bookingservice.entity.Booking;
import com.eventbooking.bookingservice.entity.BookingStatus;
import com.eventbooking.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @MockitoBean
    private EventServiceClient eventServiceClient;

    @Test
    void shouldCreateBooking() throws  Exception {
        doNothing().when(eventServiceClient).reserveSeats(1L, 2);

        BookingRequest request = new BookingRequest();
        request.setEventId(1L);
        request.setCustomerEmail("test@example.com");
        request.setSeatsBooked(2);

        mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetBookingById() throws Exception {
        Long bookingId = createTestBooking(1L, "test@example.com", 2);

        mockMvc.perform(get("/api/bookings/" + bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerEmail").value("test@example.com"));
    }

    @Test
    void shouldReturn404_whenBookingDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/bookings/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCancelBooking() throws Exception {
        Long bookingId = createTestBooking(1L, "test@example.com", 2);
        doNothing().when(eventServiceClient).releaseSeats(1L, 2);

        mockMvc.perform(post("/api/bookings/" + bookingId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private Long createTestBooking(Long eventId, String email, int seats) {
        Booking booking = new Booking();
        booking.setEventId(eventId);
        booking.setCustomerEmail(email);
        booking.setSeatsBooked(seats);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(Instant.now());
        return bookingRepository.save(booking).getId();
    }
}
