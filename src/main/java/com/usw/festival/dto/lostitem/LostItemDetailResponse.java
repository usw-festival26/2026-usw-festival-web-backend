package com.usw.festival.dto.lostitem;

import com.usw.festival.entity.LostItem;

public record LostItemDetailResponse(
        Long lostItemId,
        String name,
        String description,
        String status,
        String category,
        String imageUrl
) {
    public static LostItemDetailResponse from(LostItem lostItem) {
        return new LostItemDetailResponse(
                lostItem.getId(),
                lostItem.getName(),
                lostItem.getDescription(),
                lostItem.getStatus().getLabel(),
                lostItem.getCategory().getLabel(),
                lostItem.getImageUrl()
        );
    }
}
