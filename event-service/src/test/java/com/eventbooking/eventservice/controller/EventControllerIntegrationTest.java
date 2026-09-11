package com.eventbooking.eventservice.controller;

import com.eventbooking.eventservice.AbstractIntegrationTest;
import com.eventbooking.eventservice.dto.EventRequest;
import com.eventbooking.eventservice.dto.SeatsRequest;
import com.eventbooking.eventservice.entity.Event;
import com.eventbooking.eventservice.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldCreateEvent() throws Exception {
        EventRequest request = new EventRequest();
        request.setTitle("Jazz Concert");
        request.setEventDate(OffsetDateTime.now().plusDays(10));
        request.setLocation("Paris");
        request.setTotalSeats(100);

        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Jazz Concert"))
                .andExpect(jsonPath("$.availableSeats").value(100));
    }

    @Test
    void shouldReturn404_whenEventDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldUpdateEvent() throws Exception {
        Long eventId = createTestEvent("Original Title", "Paris", 100);

        EventRequest updateRequest = new EventRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setEventDate(OffsetDateTime.now().plusDays(15));
        updateRequest.setLocation("Lyon");
        updateRequest.setTotalSeats(150);

        mockMvc.perform(put("/api/events/" + eventId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.location").value("Lyon"))
                .andExpect(jsonPath("$.totalSeats").value(150));
    }

    @Test
    void shouldDeleteEvent() throws Exception {
        Long eventId = createTestEvent("Event To Delete", "Paris", 50);

        mockMvc.perform(delete("/api/events/" + eventId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/events/" + eventId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReserveSeats() throws Exception {
        Long eventId = createTestEvent("Event To Reserve", "Paris", 50);

        SeatsRequest seatsRequest = new SeatsRequest();
        seatsRequest.setSeats(10);

        mockMvc.perform(patch("/api/events/" + eventId + "/reserve-seats")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(seatsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSeats").value(40));
    }

    @Test
    void shouldReleaseSeats() throws Exception {
        Long eventId = createTestEvent("Event To Release", "Paris", 50);

        SeatsRequest seatsRequest = new SeatsRequest();
        seatsRequest.setSeats(5);

        mockMvc.perform(patch("/api/events/" + eventId + "/release-seats")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(seatsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSeats").value(55));
    }

    @Test
    void shouldReturn409_whenAvailableSeatsIsLowerThanReserveSeats() throws Exception {
        Long eventId = createTestEvent("Event To Reserve", "Paris", 50);
        int reserveSeats = 52;

        SeatsRequest seatsRequest = new SeatsRequest();
        seatsRequest.setSeats(reserveSeats);

        mockMvc.perform(patch("/api/events/" + eventId + "/reserve-seats")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(seatsRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    private Long createTestEvent(String title, String location, int totalSeats) {
        Event event = new Event();
        event.setTitle(title);
        event.setEventDate(OffsetDateTime.now().plusDays(10));
        event.setLocation(location);
        event.setTotalSeats(totalSeats);
        event.setAvailableSeats(totalSeats);
        event.setCreatedAt(Instant.now());
        return eventRepository.save(event).getId();
    }
}
