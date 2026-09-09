package com.eventbooking.bookingservice.client;

import com.eventbooking.bookingservice.exception.EventServiceUnavailableException;
import com.eventbooking.bookingservice.exception.SeatsUnavailableException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class EventServiceClient {

    private final RestClient eventServiceRestClient;

    public EventServiceClient(RestClient eventServiceRestClient) {
        this.eventServiceRestClient = eventServiceRestClient;
    }

    public void reserveSeats(Long eventId, int seats) {
        try {
            eventServiceRestClient.patch()
                    .uri("/api/events/{id}/reserve-seats", eventId)
                    .body(Map.of("seats", seats))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        if (response.getStatusCode().value() == 409) {
                            throw new SeatsUnavailableException(eventId, seats);
                        }
                        throw new RestClientException("Event Service rejected the request: " + response.getStatusCode());
                    })
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new EventServiceUnavailableException(eventId, e);
        }
    }

    public void releaseSeats(Long eventId, int seats) {
        try {
            eventServiceRestClient.patch()
                    .uri("/api/events/{id}/release-seats", eventId)
                    .body(Map.of("seats", seats))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            // Releasing seats is a best-effort compensation operation — log without blocking further
            // (to be enhanced in Phase 6 with proper structured logging)
        }
    }
}
