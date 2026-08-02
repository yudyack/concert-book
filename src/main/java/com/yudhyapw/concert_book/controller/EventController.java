package com.yudhyapw.concert_book.controller;

import com.yudhyapw.concert_book.dto.EventResponse;
import com.yudhyapw.concert_book.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> search(@RequestParam(required = false) String name) {
        return eventService.search(name);
    }

    @GetMapping("/{eventId}")
    public EventResponse getById(@PathVariable Long eventId) {
        return eventService.getById(eventId);
    }
}