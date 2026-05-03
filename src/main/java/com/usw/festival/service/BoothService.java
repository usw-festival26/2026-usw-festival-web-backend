package com.usw.festival.service;

import com.usw.festival.dto.booth.AdminBoothMenuResponse;
import com.usw.festival.dto.booth.BoothCreateRequest;
import com.usw.festival.dto.booth.BoothDetailResponse;
import com.usw.festival.dto.booth.BoothMenuCreateRequest;
import com.usw.festival.dto.booth.BoothMenuStatusUpdateRequest;
import com.usw.festival.dto.booth.BoothMenuResponse;
import com.usw.festival.dto.booth.BoothMenuUpdateRequest;
import com.usw.festival.dto.booth.BoothResponse;
import com.usw.festival.dto.booth.BoothUpdateRequest;
import com.usw.festival.entity.Booth;
import com.usw.festival.entity.BoothMenu;
import com.usw.festival.entity.BoothMenuStatus;
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
        return BoothDetailResponse.from(findBooth(id));
    }

    @Transactional
    public BoothDetailResponse createBooth(BoothCreateRequest request) {
        Booth booth = boothRepository.save(
                new Booth(
                        request.name(),
                        request.description(),
                        request.imageUrl(),
                        null,
                        request.college()
                )
        );
        return BoothDetailResponse.from(booth);
    }

    @Transactional
    public BoothDetailResponse updateBooth(Long id, BoothUpdateRequest request) {
        Booth booth = findBooth(id);
        booth.update(
                request.name(),
                request.description(),
                request.imageUrl(),
                null,
                request.college()
        );
        return BoothDetailResponse.from(booth);
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

    public List<AdminBoothMenuResponse> getAdminBoothMenus(Long boothId) {
        if (!boothRepository.existsById(boothId)) {
            throw new NoSuchElementException("존재하지 않는 부스입니다. id=" + boothId);
        }
        return boothMenuRepository.findAllByBoothIdOrderByIdAsc(boothId)
                .stream()
                .map(AdminBoothMenuResponse::from)
                .toList();
    }

    @Transactional
    public AdminBoothMenuResponse createBoothMenu(Long boothId, BoothMenuCreateRequest request) {
        Booth booth = findBooth(boothId);
        BoothMenu boothMenu = boothMenuRepository.save(
                new BoothMenu(
                        booth,
                        request.name(),
                        request.price(),
                        "",
                        request.imageUrl(),
                        BoothMenuStatus.ON_SALE
                )
        );
        return AdminBoothMenuResponse.from(boothMenu);
    }

    @Transactional
    public AdminBoothMenuResponse updateBoothMenu(Long boothId, Long menuId, BoothMenuUpdateRequest request) {
        BoothMenu boothMenu = findBoothMenu(boothId, menuId);
        boothMenu.update(
                request.name(),
                request.price(),
                null,
                request.imageUrl(),
                null
        );
        return AdminBoothMenuResponse.from(boothMenu);
    }

    @Transactional
    public AdminBoothMenuResponse updateBoothMenuStatus(Long boothId,
                                                        Long menuId,
                                                        BoothMenuStatusUpdateRequest request) {
        BoothMenu boothMenu = findBoothMenu(boothId, menuId);
        boothMenu.update(
                null,
                null,
                null,
                null,
                request.status()
        );
        return AdminBoothMenuResponse.from(boothMenu);
    }

    @Transactional
    public void deleteBoothMenu(Long boothId, Long menuId) {
        BoothMenu boothMenu = findBoothMenu(boothId, menuId);
        boothMenuRepository.delete(boothMenu);
    }

    private Booth findBooth(Long id) {
        return boothRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 부스입니다. id=" + id));
    }

    private BoothMenu findBoothMenu(Long boothId, Long menuId) {
        return boothMenuRepository.findByIdAndBoothId(menuId, boothId)
                .orElseThrow(() -> new NoSuchElementException(
                        "존재하지 않는 부스 메뉴입니다. boothId=" + boothId + ", menuId=" + menuId
                ));
    }
}
