package com.usw.festival.dto.lostitem;

import com.usw.festival.entity.LostItem;
import com.usw.festival.entity.LostItemStatus;

public record LostItemResponse(
        Long lostItemId,
        String name,
        LostItemStatus status,
        String imageUrl
) {
    public static LostItemResponse from(LostItem lostItem) {
        return new LostItemResponse(
                lostItem.getId(),
                lostItem.getName(),
                lostItem.getStatus(),
                lostItem.getImageUrl()
        );
    }
}
