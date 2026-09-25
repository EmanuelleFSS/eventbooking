package com.eventbooking.eventservice.messaging;

import java.time.Instant;
import java.time.OffsetDateTime;

public record CatalogEvent(
        Long eventId,
        String title,
        String location,
        OffsetDateTime eventDate,
        String changeType,
        Instant occurredAt
) {
}
