package com.usw.festival.controller;

import com.usw.festival.dto.booth.BoothCreateRequest;
import com.usw.festival.dto.booth.BoothDetailResponse;
import com.usw.festival.dto.booth.BoothResponse;
import com.usw.festival.dto.booth.BoothUpdateRequest;
import com.usw.festival.service.BoothService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping
    public ResponseEntity<List<BoothResponse>> getBooths() {
        return ResponseEntity.ok(boothService.getBooths());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoothDetailResponse> getBooth(@PathVariable Long id) {
        return ResponseEntity.ok(boothService.getBooth(id));
    }

    @PostMapping
    public ResponseEntity<BoothDetailResponse> createBooth(@Valid @RequestBody BoothCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boothService.createBooth(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoothDetailResponse> updateBooth(@PathVariable Long id,
                                                           @Valid @RequestBody BoothUpdateRequest request) {
        return ResponseEntity.ok(boothService.updateBooth(id, request));
    }
}
