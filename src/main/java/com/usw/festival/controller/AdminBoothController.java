package com.usw.festival.controller;

import com.usw.festival.dto.booth.AdminBoothMenuResponse;
import com.usw.festival.dto.booth.BoothMenuCreateRequest;
import com.usw.festival.dto.booth.BoothMenuStatusUpdateRequest;
import com.usw.festival.dto.booth.BoothMenuUpdateRequest;
import com.usw.festival.service.BoothService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/booths")
@PreAuthorize("hasRole('DEPARTMENT_COUNCIL')")
public class AdminBoothController {

    private final BoothService boothService;

    public AdminBoothController(BoothService boothService) {
        this.boothService = boothService;
    }

    @GetMapping("/{boothId}/menus")
    public ResponseEntity<List<AdminBoothMenuResponse>> getBoothMenus(@PathVariable Long boothId) {
        return ResponseEntity.ok(boothService.getAdminBoothMenus(boothId));
    }

    @PostMapping("/{boothId}/menus")
    public ResponseEntity<AdminBoothMenuResponse> createBoothMenu(@PathVariable Long boothId,
                                                                  @Valid @RequestBody BoothMenuCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boothService.createBoothMenu(boothId, request));
    }

    @PatchMapping("/{boothId}/menus/{menuId}")
    public ResponseEntity<AdminBoothMenuResponse> updateBoothMenu(@PathVariable Long boothId,
                                                                  @PathVariable Long menuId,
                                                                  @Valid @RequestBody BoothMenuUpdateRequest request) {
        return ResponseEntity.ok(boothService.updateBoothMenu(boothId, menuId, request));
    }

    @PatchMapping("/{boothId}/menus/{menuId}/status")
    public ResponseEntity<AdminBoothMenuResponse> updateBoothMenuStatus(
            @PathVariable Long boothId,
            @PathVariable Long menuId,
            @Valid @RequestBody BoothMenuStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(boothService.updateBoothMenuStatus(boothId, menuId, request));
    }

    @DeleteMapping("/{boothId}/menus/{menuId}")
    public ResponseEntity<Void> deleteBoothMenu(@PathVariable Long boothId, @PathVariable Long menuId) {
        boothService.deleteBoothMenu(boothId, menuId);
        return ResponseEntity.noContent().build();
    }
}
