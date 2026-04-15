package com.usw.festival.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usw.festival.dto.admin.AdminLoginRequest;
import com.usw.festival.dto.notice.NoticeSaveRequest;
import com.usw.festival.entity.AdminAccount;
import com.usw.festival.entity.AdminRole;
import com.usw.festival.entity.Notice;
import com.usw.festival.repository.AdminAccountRepository;
import com.usw.festival.repository.NoticeRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminNoticeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminAccountRepository adminAccountRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        noticeRepository.deleteAll();
        adminAccountRepository.deleteAll();
        adminAccountRepository.save(
                new AdminAccount(
                        "student-admin",
                        passwordEncoder.encode("student-password"),
                        AdminRole.STUDENT_COUNCIL
                )
        );
        adminAccountRepository.save(
                new AdminAccount(
                        "department-admin",
                        passwordEncoder.encode("department-password"),
                        AdminRole.DEPARTMENT_COUNCIL
                )
        );
    }

    @Test
    void unauthenticatedRequestToAdminNoticeApiReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/notices"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void departmentCouncilCannotAccessAdminNoticeApi() throws Exception {
        LoginSession loginSession = login("department-admin", "department-password");

        mockMvc.perform(get("/api/admin/notices")
                        .session(loginSession.session()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void studentCouncilCanCreateNotice() throws Exception {
        LoginSession loginSession = login("student-admin", "student-password");
        NoticeSaveRequest request = new NoticeSaveRequest("학생회 공지", "공지 본문", true);

        mockMvc.perform(post("/api/admin/notices")
                        .secure(true)
                        .session(loginSession.session())
                        .cookie(loginSession.csrfCookie())
                        .header("X-XSRF-TOKEN", loginSession.csrfCookie().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("학생회 공지"))
                .andExpect(jsonPath("$.content").value("공지 본문"))
                .andExpect(jsonPath("$.pinned").value(true));

        assertThat(noticeRepository.findAll()).hasSize(1);
    }

    @Test
    void studentCouncilCanGetNoticeListAndDetail() throws Exception {
        Notice firstNotice = noticeRepository.save(new Notice("첫 번째 공지", "첫 번째 본문", false));
        Notice secondNotice = noticeRepository.save(new Notice("두 번째 공지", "두 번째 본문", true));
        LoginSession loginSession = login("student-admin", "student-password");

        mockMvc.perform(get("/api/admin/notices")
                        .session(loginSession.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].noticeId").value(secondNotice.getId()))
                .andExpect(jsonPath("$[1].noticeId").value(firstNotice.getId()));

        mockMvc.perform(get("/api/admin/notices/{id}", secondNotice.getId())
                        .session(loginSession.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(secondNotice.getId()))
                .andExpect(jsonPath("$.title").value("두 번째 공지"))
                .andExpect(jsonPath("$.content").value("두 번째 본문"))
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    void studentCouncilCanUpdateNotice() throws Exception {
        Notice notice = noticeRepository.save(new Notice("수정 전 제목", "수정 전 본문", false));
        LoginSession loginSession = login("student-admin", "student-password");
        NoticeSaveRequest request = new NoticeSaveRequest("수정 후 제목", "수정 후 본문", true);

        mockMvc.perform(put("/api/admin/notices/{id}", notice.getId())
                        .secure(true)
                        .session(loginSession.session())
                        .cookie(loginSession.csrfCookie())
                        .header("X-XSRF-TOKEN", loginSession.csrfCookie().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(notice.getId()))
                .andExpect(jsonPath("$.title").value("수정 후 제목"))
                .andExpect(jsonPath("$.content").value("수정 후 본문"))
                .andExpect(jsonPath("$.pinned").value(true));

        Notice updatedNotice = noticeRepository.findById(notice.getId()).orElseThrow();
        assertThat(updatedNotice.getTitle()).isEqualTo("수정 후 제목");
        assertThat(updatedNotice.getContent()).isEqualTo("수정 후 본문");
        assertThat(updatedNotice.isPinned()).isTrue();
    }

    @Test
    void studentCouncilCanDeleteNotice() throws Exception {
        Notice notice = noticeRepository.save(new Notice("삭제 대상", "삭제 본문", false));
        LoginSession loginSession = login("student-admin", "student-password");

        mockMvc.perform(delete("/api/admin/notices/{id}", notice.getId())
                        .secure(true)
                        .session(loginSession.session())
                        .cookie(loginSession.csrfCookie())
                        .header("X-XSRF-TOKEN", loginSession.csrfCookie().getValue()))
                .andExpect(status().isNoContent());

        assertThat(noticeRepository.findById(notice.getId())).isEmpty();
    }

    @Test
    void createNoticeValidationFailureReturnsBadRequest() throws Exception {
        LoginSession loginSession = login("student-admin", "student-password");
        NoticeSaveRequest request = new NoticeSaveRequest("", "", null);

        mockMvc.perform(post("/api/admin/notices")
                        .secure(true)
                        .session(loginSession.session())
                        .cookie(loginSession.csrfCookie())
                        .header("X-XSRF-TOKEN", loginSession.csrfCookie().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    private LoginSession login(String loginId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                        .secure(true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest(loginId, password))))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();

        Cookie csrfCookie = extractCookie(result.getResponse(), "XSRF-TOKEN");
        assertThat(csrfCookie).isNotNull();

        return new LoginSession(session, csrfCookie);
    }

    private Cookie extractCookie(MockHttpServletResponse response, String cookieName) {
        Cookie[] cookies = response.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }

    private record LoginSession(
            MockHttpSession session,
            Cookie csrfCookie
    ) {
    }
}
