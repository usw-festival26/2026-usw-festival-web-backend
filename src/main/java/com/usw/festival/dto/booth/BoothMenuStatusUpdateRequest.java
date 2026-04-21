package com.usw.festival.dto.booth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BoothMenuStatusUpdateRequest(
        @NotBlank
        @Pattern(
                regexp = "ON_SALE|SOLD_OUT",
                message = "허용되지 않는 메뉴 상태입니다."
        )
        String status
) {
}
