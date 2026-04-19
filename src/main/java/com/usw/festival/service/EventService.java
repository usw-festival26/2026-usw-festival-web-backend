package com.usw.festival.service;

import com.usw.festival.dto.event.EventResponse;
import com.usw.festival.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventResponse> getEvents() {
        return eventRepository.findAllByOrderByIdAsc()
                .stream()
                .map(EventResponse::from)
                .toList();
    }

    public EventResponse getEvent(Long id) {
        return eventRepository.findById(id)
                .map(EventResponse::from)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 이벤트입니다. id=" + id));
    }
}
