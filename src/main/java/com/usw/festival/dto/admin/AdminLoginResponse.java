package com.usw.festival.dto.admin;

import com.usw.festival.entity.AdminRole;

public record AdminLoginResponse(
        String role
) {
    public static AdminLoginResponse from(AdminRole role) {
        return new AdminLoginResponse(role.name());
    }
}
