package com.usw.festival.repository;

import com.usw.festival.entity.BoothMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoothMenuRepository extends JpaRepository<BoothMenu, Long> {

    List<BoothMenu> findAllByBoothIdOrderByIdAsc(Long boothId);
}
