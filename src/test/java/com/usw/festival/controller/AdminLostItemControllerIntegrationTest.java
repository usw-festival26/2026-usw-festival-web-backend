package com.usw.festival.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usw.festival.dto.lostitem.LostItemCreateRequest;
import com.usw.festival.dto.lostitem.LostItemUpdateRequest;
import com.usw.festival.entity.LostItem;
import com.usw.festival.entity.LostItemCategory;
import com.usw.festival.entity.LostItemStatus;
import com.usw.festival.repository.LostItemRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminLostItemControllerIntegrationTest {

    private static final String TEST_CSRF_TOKEN = "test-csrf-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LostItemRepository lostItemRepository;

    @BeforeEach
    void setUp() {
        lostItemRepository.deleteAll();
    }

    @Test
    void unauthenticatedRequestToAdminLostItemApiReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/lost-items")
                        .secure(true)
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LostItemCreateRequest("검은색 지갑", "학생증과 카드가 들어 있음", null, null)
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void departmentCouncilCannotAccessAdminLostItemApi() throws Exception {
        mockMvc.perform(post("/api/admin/lost-items")
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LostItemCreateRequest("검은색 지갑", "학생증과 카드가 들어 있음", null, null)
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void departmentCouncilCannotAccessAdminLostItemListApi() throws Exception {
        mockMvc.perform(get("/api/admin/lost-items")
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void studentCouncilCanGetAdminLostItemListAndDetail() throws Exception {
        LostItem firstLostItem = lostItemRepository.save(
                new LostItem(
                        "검은색 지갑",
                        "학생증과 카드가 들어 있음",
                        LostItemStatus.STORED,
                        LostItemCategory.WALLET_CARD,
                        "https://example.com/wallet.jpg"
                )
        );
        LostItem secondLostItem = lostItemRepository.save(
                new LostItem(
                        "무선 이어폰",
                        "검은색 케이스 포함",
                        LostItemStatus.CLAIMED,
                        LostItemCategory.ELECTRONICS,
                        null
                )
        );

        mockMvc.perform(get("/api/admin/lost-items")
                        .secure(true)
                        .with(user("student-admin").roles("STUDENT_COUNCIL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].lostItemId").value(secondLostItem.getId()))
                .andExpect(jsonPath("$[0].status").value("수령 완료"))
                .andExpect(jsonPath("$[0].category").value("전자기기"))
                .andExpect(jsonPath("$[1].lostItemId").value(firstLostItem.getId()))
                .andExpect(jsonPath("$[1].status").value("보관 중"))
                .andExpect(jsonPath("$[1].category").value("지갑/카드"));

        mockMvc.perform(get("/api/admin/lost-items/{id}", firstLostItem.getId())
                        .secure(true)
                        .with(user("student-admin").roles("STUDENT_COUNCIL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lostItemId").value(firstLostItem.getId()))
                .andExpect(jsonPath("$.name").value("검은색 지갑"))
                .andExpect(jsonPath("$.description").value("학생증과 카드가 들어 있음"))
                .andExpect(jsonPath("$.status").value("보관 중"))
                .andExpect(jsonPath("$.category").value("지갑/카드"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/wallet.jpg"));
    }

    @Test
    void studentCouncilCanCreateLostItemWithDefaultCategoryAndStoredStatus() throws Exception {
        LostItemCreateRequest request = new LostItemCreateRequest(
                "검은색 지갑",
                "학생증과 카드가 들어 있음",
                null,
                "https://example.com/lost-item.jpg"
        );

        mockMvc.perform(post("/api/admin/lost-items")
                        .secure(true)
                        .with(user("student-admin").roles("STUDENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("검은색 지갑"))
                .andExpect(jsonPath("$.description").value("학생증과 카드가 들어 있음"))
                .andExpect(jsonPath("$.status").value("보관 중"))
                .andExpect(jsonPath("$.category").value("기타"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/lost-item.jpg"));

        LostItem savedLostItem = lostItemRepository.findAll().getFirst();
        assertThat(savedLostItem.getStatus()).isEqualTo(LostItemStatus.STORED);
        assertThat(savedLostItem.getCategory()).isEqualTo(LostItemCategory.OTHER);
    }

    @Test
    void studentCouncilCanUpdateLostItem() throws Exception {
        LostItem lostItem = lostItemRepository.save(
                new LostItem(
                        "검은색 지갑",
                        "학생증과 카드가 들어 있음",
                        LostItemStatus.STORED,
                        LostItemCategory.OTHER,
                        "https://example.com/old-image.jpg"
                )
        );
        LostItemUpdateRequest request = new LostItemUpdateRequest(
                "무선 이어폰",
                "검은색 케이스 포함",
                "ELECTRONICS",
                "CLAIMED",
                null
        );

        mockMvc.perform(put("/api/admin/lost-items/{id}", lostItem.getId())
                        .secure(true)
                        .with(user("student-admin").roles("STUDENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lostItemId").value(lostItem.getId()))
                .andExpect(jsonPath("$.name").value("무선 이어폰"))
                .andExpect(jsonPath("$.description").value("검은색 케이스 포함"))
                .andExpect(jsonPath("$.status").value("수령 완료"))
                .andExpect(jsonPath("$.category").value("전자기기"))
                .andExpect(jsonPath("$.imageUrl").doesNotExist());

        LostItem updatedLostItem = lostItemRepository.findById(lostItem.getId()).orElseThrow();
        assertThat(updatedLostItem.getName()).isEqualTo("무선 이어폰");
        assertThat(updatedLostItem.getDescription()).isEqualTo("검은색 케이스 포함");
        assertThat(updatedLostItem.getStatus()).isEqualTo(LostItemStatus.CLAIMED);
        assertThat(updatedLostItem.getCategory()).isEqualTo(LostItemCategory.ELECTRONICS);
        assertThat(updatedLostItem.getImageUrl()).isNull();
    }

    @Test
    void createLostItemValidationFailureReturnsBadRequest() throws Exception {
        LostItemCreateRequest request = new LostItemCreateRequest("", "", null, null);

        mockMvc.perform(post("/api/admin/lost-items")
                        .secure(true)
                        .with(user("student-admin").roles("STUDENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void invalidCategoryOrStatusReturnsValidationError() throws Exception {
        LostItem lostItem = lostItemRepository.save(
                new LostItem(
                        "검은색 지갑",
                        "학생증과 카드가 들어 있음",
                        LostItemStatus.STORED,
                        LostItemCategory.OTHER,
                        null
                )
        );
        LostItemUpdateRequest request = new LostItemUpdateRequest(
                "무선 이어폰",
                "검은색 케이스 포함",
                "INVALID_CATEGORY",
                "INVALID_STATUS",
                null
        );

        mockMvc.perform(put("/api/admin/lost-items/{id}", lostItem.getId())
                        .secure(true)
                        .with(user("student-admin").roles("STUDENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("category")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("status")));
    }

    @Test
    void publicLostItemApisIncludeCategoryLabel() throws Exception {
        LostItem lostItem = lostItemRepository.save(
                new LostItem(
                        "검은색 지갑",
                        "학생증과 카드가 들어 있음",
                        LostItemStatus.CLAIMED,
                        LostItemCategory.WALLET_CARD,
                        "https://example.com/lost-item.jpg"
                )
        );

        mockMvc.perform(get("/api/lost-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lostItemId").value(lostItem.getId()))
                .andExpect(jsonPath("$[0].status").value("수령 완료"))
                .andExpect(jsonPath("$[0].category").value("지갑/카드"));

        mockMvc.perform(get("/api/lost-items/{id}", lostItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lostItemId").value(lostItem.getId()))
                .andExpect(jsonPath("$.status").value("수령 완료"))
                .andExpect(jsonPath("$.category").value("지갑/카드"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/lost-item.jpg"));
    }

    private Cookie csrfCookie() {
        Cookie cookie = new Cookie("XSRF-TOKEN", TEST_CSRF_TOKEN);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }
}
