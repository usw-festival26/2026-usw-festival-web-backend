package com.usw.festival.dto.notice;

import com.usw.festival.entity.Notice;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long noticeId,
        String title,
        boolean pinned,
        LocalDateTime createdAt
) {
    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.isPinned(),
                notice.getCreatedAt()
        );
    }
}
