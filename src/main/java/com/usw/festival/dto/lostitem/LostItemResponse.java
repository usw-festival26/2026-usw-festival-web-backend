package com.usw.festival.dto.lostitem;

import com.usw.festival.entity.LostItem;

public record LostItemResponse(
        Long lostItemId,
        String name,
        String status,
        String category,
        String imageUrl
) {
    public static LostItemResponse from(LostItem lostItem) {
        return new LostItemResponse(
                lostItem.getId(),
                lostItem.getName(),
                lostItem.getStatus().getLabel(),
                lostItem.getCategory().getLabel(),
                lostItem.getImageUrl()
        );
    }
}
