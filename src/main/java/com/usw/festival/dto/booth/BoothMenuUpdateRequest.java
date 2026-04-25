package com.usw.festival.dto.booth;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record BoothMenuUpdateRequest(
        @Pattern(regexp = ".*\\S.*", message = "공백일 수 없습니다.")
        @Size(max = 60)
        String name,

        @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
        Integer price,

        @Pattern(regexp = ".*\\S.*", message = "공백일 수 없습니다.")
        @URL(message = "올바른 URL 형식이어야 합니다.")
        @Size(max = 2048)
        String imageUrl
) {
}
