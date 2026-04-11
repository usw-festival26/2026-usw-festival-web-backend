package com.usw.festival.dto.lostitem;

import com.usw.festival.entity.LostItem;
import com.usw.festival.entity.LostItemStatus;

public record LostItemDetailResponse(
        Long lostItemId,
        String name,
        String description,
        String status,
        String imageUrl
) {
    public static LostItemDetailResponse from(LostItem lostItem) {
        return new LostItemDetailResponse(
                lostItem.getId(),
                lostItem.getName(),
                lostItem.getDescription(),
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
