package com.usw.festival.dto.booth;

import com.usw.festival.entity.College;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record BoothUpdateRequest(
        @Pattern(regexp = ".*\\S.*", message = "공백일 수 없습니다.")
        @Size(max = 50)
        String name,

        @Pattern(regexp = ".*\\S.*", message = "공백일 수 없습니다.")
        @Size(max = 1000)
        String description,

        @Pattern(regexp = ".*\\S.*", message = "공백일 수 없습니다.")
        @URL(message = "올바른 URL 형식이어야 합니다.")
        @Size(max = 2048)
        String imageUrl,

        College college
) {
}
