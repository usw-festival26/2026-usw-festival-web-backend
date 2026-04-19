package com.usw.festival.service;

import com.usw.festival.dto.artist.ArtistResponse;
import com.usw.festival.repository.ArtistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ArtistService {

    private final ArtistRepository artistRepository;

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    public List<ArtistResponse> getArtists() {
        return artistRepository.findAllByOrderByIdAsc()
                .stream()
                .map(ArtistResponse::from)
                .toList();
    }

    public ArtistResponse getArtist(Long id) {
        return artistRepository.findById(id)
                .map(ArtistResponse::from)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 아티스트입니다. id=" + id));
    }
}
