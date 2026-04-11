package com.usw.festival.controller;

import com.usw.festival.dto.booth.BoothDetailResponse;
import com.usw.festival.dto.booth.BoothMenuResponse;
import com.usw.festival.dto.booth.BoothResponse;
import com.usw.festival.service.BoothService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/booths")
public class BoothController {

    private final BoothService boothService;

    public BoothController(BoothService boothService) {
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

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<BoothMenuResponse>> getBoothMenus(@PathVariable Long id) {
        return ResponseEntity.ok(boothService.getBoothMenus(id));
    }
}
