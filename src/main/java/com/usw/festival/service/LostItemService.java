package com.usw.festival.service;

import com.usw.festival.dto.lostitem.LostItemCreateRequest;
import com.usw.festival.dto.lostitem.LostItemDetailResponse;
import com.usw.festival.dto.lostitem.LostItemResponse;
import com.usw.festival.dto.lostitem.LostItemUpdateRequest;
import com.usw.festival.entity.LostItem;
import com.usw.festival.entity.LostItemCategory;
import com.usw.festival.entity.LostItemStatus;
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
        return LostItemDetailResponse.from(findLostItem(id));
    }

    @Transactional
    public LostItemDetailResponse createLostItem(LostItemCreateRequest request) {
        LostItem lostItem = lostItemRepository.save(
                new LostItem(
                        request.name(),
                        request.description(),
                        LostItemStatus.STORED,
                        toCategory(request.category()),
                        request.imageUrl()
                )
        );
        return LostItemDetailResponse.from(lostItem);
    }

    @Transactional
    public LostItemDetailResponse updateLostItem(Long id, LostItemUpdateRequest request) {
        LostItem lostItem = findLostItem(id);
        lostItem.update(
                request.name(),
                request.description(),
                LostItemStatus.from(request.status()),
                LostItemCategory.from(request.category()),
                request.imageUrl()
        );
        return LostItemDetailResponse.from(lostItem);
    }

    private LostItem findLostItem(Long id) {
        return lostItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 분실물입니다. id=" + id));
    }

    private LostItemCategory toCategory(String category) {
        if (category == null) {
            return null;
        }
        return LostItemCategory.from(category);
    }
}
