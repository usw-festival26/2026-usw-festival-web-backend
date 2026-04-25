package com.usw.festival.dto.booth;

import com.usw.festival.entity.BoothMenuStatus;
import jakarta.validation.constraints.NotNull;

public record BoothMenuStatusUpdateRequest(
        @NotNull(message = "메뉴 상태는 필수입니다.")
        BoothMenuStatus status
) {
}
