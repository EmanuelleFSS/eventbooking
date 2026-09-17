package com.eventbooking.searchservice.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;

@Document(collection = "events")
@Getter
@Setter
@NoArgsConstructor
public class EventSearchDocument {

    @Id
    private String id;
    private String title;
    private String location;
    private OffsetDateTime eventDate;
    private int bookingsCount;
}
