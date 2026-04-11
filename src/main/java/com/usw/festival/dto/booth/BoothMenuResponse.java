package com.usw.festival.dto.booth;

import com.usw.festival.entity.BoothMenu;

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
                menu.getStatus().getLabel()
        );
    }
}
