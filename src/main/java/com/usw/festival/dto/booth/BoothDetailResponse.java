package com.usw.festival.dto.booth;

import com.usw.festival.entity.Booth;

public record BoothDetailResponse(
        Long boothId,
        String name,
        String description,
        String imageUrl,
        String notice
) {
    public static BoothDetailResponse from(Booth booth) {
        return new BoothDetailResponse(
                booth.getId(),
                booth.getName(),
                booth.getDescription(),
                booth.getImageUrl(),
                booth.getNotice()
        );
    }
}
