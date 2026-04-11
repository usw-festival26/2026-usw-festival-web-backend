package com.usw.festival.repository;

import com.usw.festival.entity.Booth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoothRepository extends JpaRepository<Booth, Long> {

    List<Booth> findAllByOrderByIdAsc();
}
