package com.eventbooking.searchservice.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "processed_booking_events")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedBookingEventDocument {
    @Id
    private String id;
}
