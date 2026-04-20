package com.usw.festival.controller;

import com.usw.festival.dto.lostitem.LostItemCreateRequest;
import com.usw.festival.dto.lostitem.LostItemDetailResponse;
import com.usw.festival.dto.lostitem.LostItemResponse;
import com.usw.festival.dto.lostitem.LostItemUpdateRequest;
import com.usw.festival.service.LostItemService;
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
@RequestMapping("/api/admin/lost-items")
@PreAuthorize("hasRole('STUDENT_COUNCIL')")
public class AdminLostItemController {

    private final LostItemService lostItemService;

    public AdminLostItemController(LostItemService lostItemService) {
        this.lostItemService = lostItemService;
    }

    @GetMapping
    public ResponseEntity<List<LostItemResponse>> getLostItems() {
        return ResponseEntity.ok(lostItemService.getLostItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LostItemDetailResponse> getLostItem(@PathVariable Long id) {
        return ResponseEntity.ok(lostItemService.getLostItem(id));
    }

    @PostMapping
    public ResponseEntity<LostItemDetailResponse> createLostItem(@Valid @RequestBody LostItemCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lostItemService.createLostItem(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LostItemDetailResponse> updateLostItem(@PathVariable Long id,
                                                                 @Valid @RequestBody LostItemUpdateRequest request) {
        return ResponseEntity.ok(lostItemService.updateLostItem(id, request));
    }
}
