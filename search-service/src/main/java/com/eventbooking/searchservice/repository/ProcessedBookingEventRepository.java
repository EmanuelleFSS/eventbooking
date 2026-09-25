package com.eventbooking.searchservice.repository;

import com.eventbooking.searchservice.document.ProcessedBookingEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedBookingEventRepository extends MongoRepository<ProcessedBookingEventDocument, String> {
}