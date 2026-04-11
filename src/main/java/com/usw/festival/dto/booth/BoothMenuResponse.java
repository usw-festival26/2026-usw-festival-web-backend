package com.usw.festival.dto.booth;

import com.usw.festival.entity.BoothMenu;
import com.usw.festival.entity.BoothMenuStatus;

public record BoothMenuResponse(
        Long menuId,
        String name,
        Integer price,
        String imageUrl,
        String status
) {
    public static BoothMenuResponse from(BoothMenu menu) {
        return new BoothMenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getImageUrl(),
                toLabel(menu.getStatus())
        );
    }

    private static String toLabel(BoothMenuStatus status) {
        return switch (status) {
            case ON_SALE -> "판매 중";
            case SOLD_OUT -> "품절";
        };
    }
}
