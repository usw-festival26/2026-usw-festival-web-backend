package com.usw.festival.dto.lostitem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record LostItemUpdateRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @Size(max = 2000)
        String description,

        @NotBlank
        @Pattern(
                regexp = "ELECTRONICS|WALLET_CARD|CLOTHING_BAG|OTHER|전자기기|지갑/카드|의류/가방|기타",
                message = "허용되지 않는 분실물 카테고리입니다."
        )
        String category,

        @NotBlank
        @Pattern(
                regexp = "STORED|CLAIMED|보관 중|수령 완료",
                message = "허용되지 않는 분실물 상태입니다."
        )
        String status,

        @URL(message = "올바른 URL 형식이어야 합니다.")
        @Size(max = 2048)
        String imageUrl
) {
}
