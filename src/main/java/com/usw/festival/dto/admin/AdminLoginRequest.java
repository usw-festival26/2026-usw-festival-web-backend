package com.usw.festival.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank String loginId,
        @NotBlank String password
) {
}
