package com.usw.festival.controller;

import com.usw.festival.entity.Notice;
import com.usw.festival.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NoticeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoticeRepository noticeRepository;

    @BeforeEach
    void setUp() {
        noticeRepository.deleteAll();
    }

    @Test
    void noticeListIncludesContent() throws Exception {
        Notice firstNotice = noticeRepository.save(new Notice("첫 번째 공지", "첫 번째 본문", false));
        Notice secondNotice = noticeRepository.save(new Notice("두 번째 공지", "두 번째 본문", true));

        mockMvc.perform(get("/api/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].noticeId").value(secondNotice.getId()))
                .andExpect(jsonPath("$[0].content").value("두 번째 본문"))
                .andExpect(jsonPath("$[1].noticeId").value(firstNotice.getId()))
                .andExpect(jsonPath("$[1].content").value("첫 번째 본문"));
    }

    @Test
    void noticeDetailIsNotSupported() throws Exception {
        Notice notice = noticeRepository.save(new Notice("상세 제거 공지", "상세 제거 본문", false));

        mockMvc.perform(get("/api/notices/{id}", notice.getId()))
                .andExpect(status().isNotFound());
    }
}
