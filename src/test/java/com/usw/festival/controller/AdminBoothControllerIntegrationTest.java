package com.usw.festival.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usw.festival.dto.booth.BoothCreateRequest;
import com.usw.festival.dto.booth.BoothUpdateRequest;
import com.usw.festival.entity.Booth;
import com.usw.festival.repository.BoothRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminBoothControllerIntegrationTest {

    private static final String TEST_CSRF_TOKEN = "test-csrf-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoothRepository boothRepository;

    @BeforeEach
    void setUp() {
        boothRepository.deleteAll();
    }

    @Test
    void unauthenticatedRequestToAdminBoothApiReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/booths")
                        .secure(true))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void studentCouncilCannotAccessAdminBoothApi() throws Exception {
        mockMvc.perform(post("/api/admin/booths")
                        .secure(true)
                        .with(user("student-admin").roles("STUDENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BoothCreateRequest("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg")
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void departmentCouncilCanGetAdminBoothListAndDetail() throws Exception {
        Booth firstBooth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth-1.jpg", "재료 소진 시 조기 마감")
        );
        Booth secondBooth = boothRepository.save(
                new Booth("전자공학과", "음료 판매", "https://example.com/booth-2.jpg", null)
        );

        mockMvc.perform(get("/api/admin/booths")
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].boothId").value(firstBooth.getId()))
                .andExpect(jsonPath("$[0].name").value("컴퓨터학부"))
                .andExpect(jsonPath("$[1].boothId").value(secondBooth.getId()))
                .andExpect(jsonPath("$[1].name").value("전자공학과"));

        mockMvc.perform(get("/api/admin/booths/{id}", firstBooth.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boothId").value(firstBooth.getId()))
                .andExpect(jsonPath("$.name").value("컴퓨터학부"))
                .andExpect(jsonPath("$.description").value("분식 판매"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/booth-1.jpg"))
                .andExpect(jsonPath("$.notice").value("재료 소진 시 조기 마감"));
    }

    @Test
    void departmentCouncilCanCreateBooth() throws Exception {
        BoothCreateRequest request = new BoothCreateRequest(
                "컴퓨터학부",
                "분식 판매",
                "https://example.com/booth.jpg"
        );

        mockMvc.perform(post("/api/admin/booths")
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("컴퓨터학부"))
                .andExpect(jsonPath("$.description").value("분식 판매"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/booth.jpg"))
                .andExpect(jsonPath("$.notice").doesNotExist());

        Booth savedBooth = boothRepository.findAll().getFirst();
        assertThat(savedBooth.getName()).isEqualTo("컴퓨터학부");
        assertThat(savedBooth.getDescription()).isEqualTo("분식 판매");
        assertThat(savedBooth.getImageUrl()).isEqualTo("https://example.com/booth.jpg");
        assertThat(savedBooth.getNotice()).isNull();
    }

    @Test
    void createBoothValidationFailureReturnsBadRequest() throws Exception {
        BoothCreateRequest request = new BoothCreateRequest("", "", "");

        mockMvc.perform(post("/api/admin/booths")
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("name")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("description")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("imageUrl")));
    }

    @Test
    void invalidImageUrlOnCreateReturnsValidationError() throws Exception {
        BoothCreateRequest request = new BoothCreateRequest(
                "컴퓨터학부",
                "분식 판매",
                "not-a-url"
        );

        mockMvc.perform(post("/api/admin/booths")
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("imageUrl")));
    }

    @Test
    void departmentCouncilCanUpdateOnlyChangedFieldsAndKeepsOthers() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", "재료 소진 시 조기 마감")
        );
        BoothUpdateRequest request = new BoothUpdateRequest(
                "컴퓨터공학과",
                null,
                null
        );

        mockMvc.perform(put("/api/admin/booths/{id}", booth.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boothId").value(booth.getId()))
                .andExpect(jsonPath("$.name").value("컴퓨터공학과"))
                .andExpect(jsonPath("$.description").value("분식 판매"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/booth.jpg"))
                .andExpect(jsonPath("$.notice").value("재료 소진 시 조기 마감"));

        Booth updatedBooth = boothRepository.findById(booth.getId()).orElseThrow();
        assertThat(updatedBooth.getName()).isEqualTo("컴퓨터공학과");
        assertThat(updatedBooth.getDescription()).isEqualTo("분식 판매");
        assertThat(updatedBooth.getImageUrl()).isEqualTo("https://example.com/booth.jpg");
        assertThat(updatedBooth.getNotice()).isEqualTo("재료 소진 시 조기 마감");
    }

    @Test
    void emptyUpdateRequestKeepsExistingFields() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", "재료 소진 시 조기 마감")
        );

        mockMvc.perform(put("/api/admin/booths/{id}", booth.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boothId").value(booth.getId()))
                .andExpect(jsonPath("$.name").value("컴퓨터학부"))
                .andExpect(jsonPath("$.description").value("분식 판매"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/booth.jpg"))
                .andExpect(jsonPath("$.notice").value("재료 소진 시 조기 마감"));

        Booth updatedBooth = boothRepository.findById(booth.getId()).orElseThrow();
        assertThat(updatedBooth.getName()).isEqualTo("컴퓨터학부");
        assertThat(updatedBooth.getDescription()).isEqualTo("분식 판매");
        assertThat(updatedBooth.getImageUrl()).isEqualTo("https://example.com/booth.jpg");
        assertThat(updatedBooth.getNotice()).isEqualTo("재료 소진 시 조기 마감");
    }

    @Test
    void blankFieldOnUpdateReturnsValidationError() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        BoothUpdateRequest request = new BoothUpdateRequest(
                " ",
                null,
                null
        );

        mockMvc.perform(put("/api/admin/booths/{id}", booth.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("name")));
    }

    @Test
    void getUnknownBoothReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/booths/999")
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void updateUnknownBoothReturnsNotFound() throws Exception {
        BoothUpdateRequest request = new BoothUpdateRequest("새 이름", null, null);

        mockMvc.perform(put("/api/admin/booths/999")
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private Cookie csrfCookie() {
        Cookie cookie = new Cookie("XSRF-TOKEN", TEST_CSRF_TOKEN);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }
}
