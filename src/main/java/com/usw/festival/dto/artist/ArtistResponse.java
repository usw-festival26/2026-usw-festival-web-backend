package com.usw.festival.dto.artist;

import com.usw.festival.entity.Artist;

public record ArtistResponse(
        Long artistId,
        String name,
        String description,
        String imageUrl
) {
    public static ArtistResponse from(Artist artist) {
        return new ArtistResponse(
                artist.getId(),
                artist.getName(),
                artist.getDescription(),
                artist.getImageUrl()
        );
    }
}
