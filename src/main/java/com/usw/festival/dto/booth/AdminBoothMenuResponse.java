package com.usw.festival.dto.booth;

import com.usw.festival.entity.BoothMenu;

public record AdminBoothMenuResponse(
        Long menuId,
        String name,
        Integer price,
        String imageUrl,
        String status
) {
    public static AdminBoothMenuResponse from(BoothMenu menu) {
        return new AdminBoothMenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getImageUrl(),
                menu.getStatus().name()
        );
    }
}
