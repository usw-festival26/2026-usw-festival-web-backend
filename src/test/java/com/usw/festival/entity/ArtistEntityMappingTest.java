package com.usw.festival.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ArtistEntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void artistCanBePersisted() {
        Artist artist = new Artist("잔나비", "축제 메인 아티스트", "https://example.com/artist.jpg");

        entityManager.persist(artist);
        entityManager.flush();
        entityManager.clear();

        Artist savedArtist = entityManager.find(Artist.class, artist.getId());

        assertThat(savedArtist).isNotNull();
        assertThat(savedArtist.getName()).isEqualTo("잔나비");
        assertThat(savedArtist.getDescription()).isEqualTo("축제 메인 아티스트");
    }
}
