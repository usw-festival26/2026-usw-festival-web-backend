package com.usw.festival.dto.lostitem;

import com.usw.festival.entity.LostItem;
import com.usw.festival.entity.LostItemStatus;

public record LostItemResponse(
        Long lostItemId,
        String name,
        String status,
        String imageUrl
) {
    public static LostItemResponse from(LostItem lostItem) {
        return new LostItemResponse(
                lostItem.getId(),
                lostItem.getName(),
                toLabel(lostItem.getStatus()),
                lostItem.getImageUrl()
        );
    }

    private static String toLabel(LostItemStatus status) {
        return switch (status) {
            case STORED -> "보관 중";
            case CLAIMED -> "수령 완료";
        };
    }
}
