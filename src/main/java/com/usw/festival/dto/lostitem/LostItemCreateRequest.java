package com.usw.festival.dto.lostitem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LostItemCreateRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @Size(max = 2000)
        String description,

        @Pattern(
                regexp = "ELECTRONICS|WALLET_CARD|CLOTHING_BAG|OTHER",
                message = "허용되지 않는 분실물 카테고리입니다."
        )
        String category,

        @Size(max = 2048)
        String imageUrl
) {
}
