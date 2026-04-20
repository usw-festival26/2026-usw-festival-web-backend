package com.usw.festival.dto.event;

import com.usw.festival.entity.Event;
import com.usw.festival.entity.EventStatus;

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
                toStatusLabel(event.getStatus()),
                event.getCreatedAt()
        );
    }

    private static String toStatusLabel(EventStatus status) {
        return switch (status) {
            case ONGOING -> "진행 중";
            case COMPLETED -> "종료";
        };
    }
}
