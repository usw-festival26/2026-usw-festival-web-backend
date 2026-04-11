package com.usw.festival.service;

import com.usw.festival.dto.lostitem.LostItemDetailResponse;
import com.usw.festival.dto.lostitem.LostItemResponse;
import com.usw.festival.repository.LostItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class LostItemService {

    private final LostItemRepository lostItemRepository;

    public LostItemService(LostItemRepository lostItemRepository) {
        this.lostItemRepository = lostItemRepository;
    }

    public List<LostItemResponse> getLostItems() {
        return lostItemRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(LostItemResponse::from)
                .toList();
    }

    public LostItemDetailResponse getLostItem(Long id) {
        return lostItemRepository.findById(id)
                .map(LostItemDetailResponse::from)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 분실물입니다. id=" + id));
    }
}
