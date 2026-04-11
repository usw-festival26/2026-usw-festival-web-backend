package com.usw.festival.controller;

import com.usw.festival.dto.lostitem.LostItemDetailResponse;
import com.usw.festival.dto.lostitem.LostItemResponse;
import com.usw.festival.service.LostItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lost-items")
public class LostItemController {

    private final LostItemService lostItemService;

    public LostItemController(LostItemService lostItemService) {
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
}
