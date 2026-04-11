package com.usw.festival.service;

import com.usw.festival.dto.booth.BoothDetailResponse;
import com.usw.festival.dto.booth.BoothMenuResponse;
import com.usw.festival.dto.booth.BoothResponse;
import com.usw.festival.repository.BoothMenuRepository;
import com.usw.festival.repository.BoothRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class BoothService {

    private final BoothRepository boothRepository;
    private final BoothMenuRepository boothMenuRepository;

    public BoothService(BoothRepository boothRepository, BoothMenuRepository boothMenuRepository) {
        this.boothRepository = boothRepository;
        this.boothMenuRepository = boothMenuRepository;
    }

    public List<BoothResponse> getBooths() {
        return boothRepository.findAllByOrderByIdAsc()
                .stream()
                .map(BoothResponse::from)
                .toList();
    }

    public BoothDetailResponse getBooth(Long id) {
        return boothRepository.findById(id)
                .map(BoothDetailResponse::from)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 부스입니다. id=" + id));
    }

    public List<BoothMenuResponse> getBoothMenus(Long boothId) {
        if (!boothRepository.existsById(boothId)) {
            throw new NoSuchElementException("존재하지 않는 부스입니다. id=" + boothId);
        }
        return boothMenuRepository.findAllByBoothIdOrderByIdAsc(boothId)
                .stream()
                .map(BoothMenuResponse::from)
                .toList();
    }
}
