package com.usw.festival.controller;

import com.usw.festival.dto.artist.ArtistResponse;
import com.usw.festival.service.ArtistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping
    public ResponseEntity<List<ArtistResponse>> getArtists() {
        return ResponseEntity.ok(artistService.getArtists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> getArtist(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.getArtist(id));
    }
}
