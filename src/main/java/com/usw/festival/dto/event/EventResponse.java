package com.usw.festival.dto.event;

import com.usw.festival.entity.Event;

import java.time.LocalDateTime;

public record EventResponse(
        Long eventId,
        String title,
        String description,
        String imageUrl,
        String status,
        LocalDateTime createdAt
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getImageUrl(),
                event.getStatus().name().toLowerCase(),
                event.getCreatedAt()
        );
    }
}
