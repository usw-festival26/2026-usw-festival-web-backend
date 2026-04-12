package com.usw.festival.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usw.festival.dto.admin.AdminLoginRequest;
import com.usw.festival.entity.AdminAccount;
import com.usw.festival.entity.AdminRole;
import com.usw.festival.repository.AdminAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AdminAuthControllerIntegrationTest.TestAdminProtectedController.class)
class AdminAuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminAccountRepository adminAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
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
    void loginReturnsRoleAndIssuesSessionAndCsrfCookies() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                        .secure(true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest("student-admin", "student-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("STUDENT_COUNCIL"))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNotNull();
        assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                .anyMatch(header -> header.contains("XSRF-TOKEN="));
    }

    @Test
    void loginFailureReturnsUnauthorizedJsonWithoutLeakingAccountExistence() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .secure(true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest("student-admin", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."))
                .andExpect(jsonPath("$.path").value("/api/admin/auth/login"));
    }

    @Test
    void loginValidationFailureKeepsCommonValidationResponseFormat() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .secure(true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest("", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("요청 값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void protectedAdminApiWithoutLoginReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/test/student"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    void mismatchedRoleReturnsForbidden() throws Exception {
        LoginSession loginSession = login("department-admin", "department-password");

        mockMvc.perform(get("/api/admin/test/student")
                        .session(loginSession.session()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    @Test
    void allowedRoleCanAccessProtectedAdminApi() throws Exception {
        LoginSession loginSession = login("student-admin", "student-password");

        mockMvc.perform(get("/api/admin/test/student")
                        .session(loginSession.session()))
                .andExpect(status().isOk());
    }

    @Test
    void csrfFailureReturnsDedicatedForbiddenResponse() throws Exception {
        LoginSession loginSession = login("student-admin", "student-password");

        mockMvc.perform(post("/api/admin/test/student-action")
                        .secure(true)
                        .session(loginSession.session())
                        .cookie(loginSession.csrfCookie()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("INVALID_CSRF_TOKEN"))
                .andExpect(jsonPath("$.message").value("CSRF 토큰이 유효하지 않습니다."));
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        LoginSession loginSession = login("student-admin", "student-password");

        mockMvc.perform(post("/api/admin/auth/logout")
                        .secure(true)
                        .session(loginSession.session())
                        .cookie(loginSession.csrfCookie())
                        .header("X-XSRF-TOKEN", loginSession.csrfCookie().getValue()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/test/student")
                        .session(loginSession.session()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validCsrfTokenAllowsStateChangingAdminRequest() throws Exception {
        LoginSession loginSession = login("student-admin", "student-password");

        mockMvc.perform(post("/api/admin/test/student-action")
                        .secure(true)
                        .session(loginSession.session())
                        .cookie(loginSession.csrfCookie())
                        .header("X-XSRF-TOKEN", loginSession.csrfCookie().getValue()))
                .andExpect(status().isOk());
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

    @RestController
    @RequestMapping("/api/admin/test")
    static class TestAdminProtectedController {

        @GetMapping("/student")
        @PreAuthorize("hasRole('STUDENT_COUNCIL')")
        public String studentOnly() {
            return "student";
        }

        @GetMapping("/department")
        @PreAuthorize("hasRole('DEPARTMENT_COUNCIL')")
        public String departmentOnly() {
            return "department";
        }

        @PostMapping("/student-action")
        @PreAuthorize("hasRole('STUDENT_COUNCIL')")
        public String studentAction() {
            return "ok";
        }
    }
}
