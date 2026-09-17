package com.eventbooking.searchservice.controller;

import com.eventbooking.searchservice.AbstractIntegrationTest;
import com.eventbooking.searchservice.document.EventSearchDocument;
import com.eventbooking.searchservice.repository.EventSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SearchControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventSearchRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void shouldReturnAllEvents_whenNoQueryProvided() throws Exception {
        createTestDocument("1", "Jazz Concert", "Paris");
        createTestDocument("2", "Rock Festival", "Lyon");

        mockMvc.perform(get("/api/search/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnMatchingEvents_whenQueryProvided() throws Exception {
        createTestDocument("1", "Jazz Concert", "Paris");
        createTestDocument("2", "Rock Festival", "Lyon");

        mockMvc.perform(get("/api/search/events").param("q", "jazz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Jazz Concert"));
    }

    @Test
    void shouldReturnEmptyList_whenNoEventMatchesQuery() throws Exception {
        createTestDocument("1", "Jazz Concert", "Paris");

        mockMvc.perform(get("/api/search/events").param("q", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private void createTestDocument(String id, String title, String location) {
        EventSearchDocument document = new EventSearchDocument();
        document.setId(id);
        document.setTitle(title);
        document.setLocation(location);
        document.setEventDate(Instant.now().plus(10, java.time.temporal.ChronoUnit.DAYS));
        repository.save(document);
    }
}
