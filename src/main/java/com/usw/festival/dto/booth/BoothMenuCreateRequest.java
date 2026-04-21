package com.usw.festival.dto.booth;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record BoothMenuCreateRequest(
        @NotBlank
        @Size(max = 60)
        String name,

        @NotNull
        @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
        Integer price,

        @NotBlank
        @URL(message = "올바른 URL 형식이어야 합니다.")
        @Size(max = 2048)
        String imageUrl
) {
}
