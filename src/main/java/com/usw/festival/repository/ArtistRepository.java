package com.usw.festival.repository;

import com.usw.festival.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    List<Artist> findAllByOrderByIdAsc();
}
