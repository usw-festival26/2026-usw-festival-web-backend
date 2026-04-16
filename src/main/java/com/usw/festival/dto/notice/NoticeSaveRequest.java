package com.usw.festival.dto.notice;

import com.usw.festival.validation.PlainText;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoticeSaveRequest(
        @NotBlank
        @PlainText
        @Size(max = 100)
        String title,

        @NotBlank
        @PlainText
        String content,

        @NotNull
        Boolean pinned
) {
}
