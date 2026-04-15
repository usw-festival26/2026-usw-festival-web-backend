package com.usw.festival.dto.notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoticeSaveRequest(
        @NotBlank
        @Size(max = 100)
        String title,

        @NotBlank
        String content,

        @NotNull
        Boolean pinned
) {
}
