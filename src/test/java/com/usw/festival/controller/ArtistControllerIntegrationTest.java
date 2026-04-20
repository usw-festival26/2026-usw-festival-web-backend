package com.usw.festival.controller;

import com.usw.festival.entity.Artist;
import com.usw.festival.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArtistControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArtistRepository artistRepository;

    @BeforeEach
    void setUp() {
        artistRepository.deleteAll();
    }

    @Test
    void getArtistsReturnsAllArtistsOrderedByIdAsc() throws Exception {
        artistRepository.save(new Artist("아티스트B", "두 번째 아티스트", null));
        artistRepository.save(new Artist("아티스트A", "첫 번째 아티스트", "https://example.com/a.jpg"));

        mockMvc.perform(get("/api/artists"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("아티스트B"))
                .andExpect(jsonPath("$[1].name").value("아티스트A"));
    }

    @Test
    void getArtistsReturnsEmptyListWhenNoArtistsExist() throws Exception {
        mockMvc.perform(get("/api/artists"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getArtistReturnsArtistById() throws Exception {
        Artist saved = artistRepository.save(new Artist("IU", "가수 아이유", "https://example.com/iu.jpg"));

        mockMvc.perform(get("/api/artists/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.artistId").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("IU"))
                .andExpect(jsonPath("$.description").value("가수 아이유"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/iu.jpg"));
    }

    @Test
    void getArtistWithNonExistentIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/artists/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getArtistsIsAccessibleWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/artists"))
                .andExpect(status().isOk());
    }

    @Test
    void getArtistByIdIsAccessibleWithoutLogin() throws Exception {
        Artist saved = artistRepository.save(new Artist("테스트", "설명", null));

        mockMvc.perform(get("/api/artists/{id}", saved.getId()))
                .andExpect(status().isOk());
    }
}
