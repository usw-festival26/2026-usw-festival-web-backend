package com.usw.festival.controller;

import com.usw.festival.dto.admin.AdminLoginRequest;
import com.usw.festival.dto.admin.AdminLoginResponse;
import com.usw.festival.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request,
                                                    HttpServletRequest httpServletRequest,
                                                    HttpServletResponse httpServletResponse) {
        return ResponseEntity.ok(adminAuthService.login(request, httpServletRequest, httpServletResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        adminAuthService.logout(httpServletRequest, httpServletResponse);
        return ResponseEntity.noContent().build();
    }
}
