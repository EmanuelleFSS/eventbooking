package com.eventbooking.searchservice.controller;

import com.eventbooking.searchservice.document.EventSearchDocument;
import com.eventbooking.searchservice.repository.EventSearchRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    private final EventSearchRepository repository;

    public SearchController(EventSearchRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/search/events")
    public List<EventSearchDocument> searchEvents(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) {
            return repository.findAll();
        }
        return repository.findByTitleContainingIgnoreCase(q);
    }
}
