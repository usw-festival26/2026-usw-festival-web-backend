package com.usw.festival.dto.booth;

import com.usw.festival.entity.Booth;

public record BoothResponse(
        Long boothId,
        String name,
        String imageUrl
) {
    public static BoothResponse from(Booth booth) {
        return new BoothResponse(
                booth.getId(),
                booth.getName(),
                booth.getImageUrl()
        );
    }
}
