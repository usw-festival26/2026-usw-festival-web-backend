package com.usw.festival.dto.lostitem;

import com.usw.festival.entity.LostItemCategory;
import com.usw.festival.entity.LostItem;
import com.usw.festival.entity.LostItemStatus;

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
                toLabel(lostItem.getStatus()),
                toLabel(lostItem.getCategory()),
                lostItem.getImageUrl()
        );
    }

    private static String toLabel(LostItemStatus status) {
        return status.getLabel();
    }

    private static String toLabel(LostItemCategory category) {
        return category.getLabel();
    }
}
