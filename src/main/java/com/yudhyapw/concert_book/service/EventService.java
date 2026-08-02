package com.yudhyapw.concert_book.service;

import com.yudhyapw.concert_book.dto.CreateEventRequest;
import com.yudhyapw.concert_book.dto.EventResponse;
import com.yudhyapw.concert_book.entity.Event;
import com.yudhyapw.concert_book.exception.ResourceNotFoundException;
import com.yudhyapw.concert_book.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> search(String name) {
        List<com.yudhyapw.concert_book.entity.Event> events = (name == null || name.isBlank())
                ? eventRepository.findAll()
                : eventRepository.findByNameContainingIgnoreCase(name);
        return events.stream().map(EventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getById(Long eventId) {
        return eventRepository.findById(eventId)
                .map(EventResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("event " + eventId + " not found"));
    }

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        if (!request.saleEnd().isAfter(request.saleStart())) {
            throw new IllegalArgumentException("saleEnd must be after saleStart");
        }
        Event event = new Event(request.name(), request.venue(), request.eventTime(),
                request.saleStart(), request.saleEnd(), request.ticketTotal(), request.rateLimitPerSecond());
        return EventResponse.from(eventRepository.save(event));
    }
}