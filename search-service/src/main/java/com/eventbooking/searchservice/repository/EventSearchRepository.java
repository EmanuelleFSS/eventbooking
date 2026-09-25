package com.eventbooking.searchservice.repository;

import com.eventbooking.searchservice.document.EventSearchDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventSearchRepository extends MongoRepository<EventSearchDocument, String> {
    List<EventSearchDocument> findByTitleContainingIgnoreCase(String query);
}
