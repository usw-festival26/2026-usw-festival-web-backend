package com.usw.festival.dto.lostitem;

import com.usw.festival.entity.LostItem;
import com.usw.festival.entity.LostItemStatus;

public record LostItemDetailResponse(
        Long lostItemId,
        String name,
        String description,
        LostItemStatus status,
        String imageUrl
) {
    public static LostItemDetailResponse from(LostItem lostItem) {
        return new LostItemDetailResponse(
                lostItem.getId(),
                lostItem.getName(),
                lostItem.getDescription(),
                lostItem.getStatus(),
                lostItem.getImageUrl()
        );
    }
}
