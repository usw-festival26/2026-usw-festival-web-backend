package com.usw.festival.controller;

import com.usw.festival.entity.Event;
import com.usw.festival.entity.EventStatus;
import com.usw.festival.repository.EventRepository;
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
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void getEventsReturnsAllEventsOrderedByIdAsc() throws Exception {
        eventRepository.save(new Event("이벤트B", "두 번째 이벤트", null, EventStatus.COMPLETED));
        eventRepository.save(new Event("이벤트A", "첫 번째 이벤트", "https://example.com/a.jpg", EventStatus.ONGOING));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("이벤트B"))
                .andExpect(jsonPath("$[0].status").value("종료"))
                .andExpect(jsonPath("$[1].title").value("이벤트A"))
                .andExpect(jsonPath("$[1].status").value("진행 중"));
    }

    @Test
    void getEventsReturnsEmptyListWhenNoEventsExist() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getEventReturnsEventById() throws Exception {
        Event saved = eventRepository.save(new Event("축제 오프닝", "개막식 이벤트", "https://example.com/opening.jpg", EventStatus.ONGOING));

        mockMvc.perform(get("/api/events/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventId").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("축제 오프닝"))
                .andExpect(jsonPath("$.description").value("개막식 이벤트"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/opening.jpg"))
                .andExpect(jsonPath("$.status").value("진행 중"))
                .andExpect(jsonPath("$.createdAt").isString());
    }

    @Test
    void getEventWithNonExistentIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/events/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getEventsIsAccessibleWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk());
    }

    @Test
    void getEventByIdIsAccessibleWithoutLogin() throws Exception {
        Event saved = eventRepository.save(new Event("테스트", "설명", null, EventStatus.ONGOING));

        mockMvc.perform(get("/api/events/{id}", saved.getId()))
                .andExpect(status().isOk());
    }
}
