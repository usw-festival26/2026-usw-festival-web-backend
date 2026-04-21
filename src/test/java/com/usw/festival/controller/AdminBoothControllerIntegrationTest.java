package com.usw.festival.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usw.festival.dto.booth.BoothCreateRequest;
import com.usw.festival.dto.booth.BoothMenuCreateRequest;
import com.usw.festival.dto.booth.BoothMenuStatusUpdateRequest;
import com.usw.festival.dto.booth.BoothMenuUpdateRequest;
import com.usw.festival.dto.booth.BoothUpdateRequest;
import com.usw.festival.entity.Booth;
import com.usw.festival.entity.BoothMenu;
import com.usw.festival.entity.BoothMenuStatus;
import com.usw.festival.repository.BoothMenuRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Autowired
    private BoothMenuRepository boothMenuRepository;

    @BeforeEach
    void setUp() {
        boothMenuRepository.deleteAll();
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

        mockMvc.perform(patch("/api/admin/booths/{id}", booth.getId())
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

        mockMvc.perform(patch("/api/admin/booths/{id}", booth.getId())
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

        mockMvc.perform(patch("/api/admin/booths/{id}", booth.getId())
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

        mockMvc.perform(patch("/api/admin/booths/999")
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

    @Test
    void unauthenticatedRequestToAdminBoothMenuApiReturnsUnauthorized() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );

        mockMvc.perform(post("/api/admin/booths/{boothId}/menus", booth.getId())
                        .secure(true)
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BoothMenuCreateRequest("떡볶이", 4000, "https://example.com/menu.jpg")
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void studentCouncilCannotAccessAdminBoothMenuApi() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );

        mockMvc.perform(post("/api/admin/booths/{boothId}/menus", booth.getId())
                        .secure(true)
                        .with(user("student-admin").roles("STUDENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BoothMenuCreateRequest("떡볶이", 4000, "https://example.com/menu.jpg")
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void departmentCouncilCanGetAdminBoothMenus() throws Exception {
        Booth firstBooth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        Booth secondBooth = boothRepository.save(
                new Booth("전자공학과", "음료 판매", "https://example.com/booth-2.jpg", null)
        );
        BoothMenu firstMenu = boothMenuRepository.save(
                new BoothMenu(firstBooth, "떡볶이", 4000, "", "https://example.com/menu-1.jpg", BoothMenuStatus.ON_SALE)
        );
        BoothMenu secondMenu = boothMenuRepository.save(
                new BoothMenu(firstBooth, "순대", 4500, "", "https://example.com/menu-2.jpg", BoothMenuStatus.SOLD_OUT)
        );
        boothMenuRepository.save(
                new BoothMenu(secondBooth, "커피", 3000, "", "https://example.com/menu-3.jpg", BoothMenuStatus.ON_SALE)
        );

        mockMvc.perform(get("/api/admin/booths/{boothId}/menus", firstBooth.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].menuId").value(firstMenu.getId()))
                .andExpect(jsonPath("$[0].name").value("떡볶이"))
                .andExpect(jsonPath("$[0].status").value("ON_SALE"))
                .andExpect(jsonPath("$[1].menuId").value(secondMenu.getId()))
                .andExpect(jsonPath("$[1].status").value("SOLD_OUT"));
    }

    @Test
    void getAdminBoothMenusForUnknownBoothReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/booths/999/menus")
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void departmentCouncilCanCreateBoothMenu() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        BoothMenuCreateRequest request = new BoothMenuCreateRequest(
                "떡볶이",
                4000,
                "https://example.com/menu.jpg"
        );

        mockMvc.perform(post("/api/admin/booths/{boothId}/menus", booth.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("떡볶이"))
                .andExpect(jsonPath("$.price").value(4000))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/menu.jpg"))
                .andExpect(jsonPath("$.status").value("ON_SALE"));

        BoothMenu savedMenu = boothMenuRepository.findAll().getFirst();
        assertThat(savedMenu.getBooth().getId()).isEqualTo(booth.getId());
        assertThat(savedMenu.getName()).isEqualTo("떡볶이");
        assertThat(savedMenu.getPrice()).isEqualTo(4000);
        assertThat(savedMenu.getImageUrl()).isEqualTo("https://example.com/menu.jpg");
        assertThat(savedMenu.getStatus()).isEqualTo(BoothMenuStatus.ON_SALE);
        assertThat(savedMenu.getDescription()).isEqualTo("");
    }

    @Test
    void createBoothMenuForUnknownBoothReturnsNotFound() throws Exception {
        BoothMenuCreateRequest request = new BoothMenuCreateRequest(
                "떡볶이",
                4000,
                "https://example.com/menu.jpg"
        );

        mockMvc.perform(post("/api/admin/booths/999/menus")
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

    @Test
    void createBoothMenuValidationFailureReturnsBadRequest() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        BoothMenuCreateRequest request = new BoothMenuCreateRequest("", 0, "");

        mockMvc.perform(post("/api/admin/booths/{boothId}/menus", booth.getId())
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
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("price")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("imageUrl")));
    }

    @Test
    void invalidImageUrlOnCreateBoothMenuReturnsValidationError() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        BoothMenuCreateRequest request = new BoothMenuCreateRequest("떡볶이", 4000, "not-a-url");

        mockMvc.perform(post("/api/admin/booths/{boothId}/menus", booth.getId())
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
    void departmentCouncilCanUpdateOnlyChangedBoothMenuFieldsAndKeepsOthers() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        BoothMenu boothMenu = boothMenuRepository.save(
                new BoothMenu(booth, "떡볶이", 4000, "", "https://example.com/menu.jpg", BoothMenuStatus.ON_SALE)
        );
        BoothMenuUpdateRequest request = new BoothMenuUpdateRequest("라볶이", null, null);

        mockMvc.perform(patch("/api/admin/booths/{boothId}/menus/{menuId}", booth.getId(), boothMenu.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuId").value(boothMenu.getId()))
                .andExpect(jsonPath("$.name").value("라볶이"))
                .andExpect(jsonPath("$.price").value(4000))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/menu.jpg"))
                .andExpect(jsonPath("$.status").value("ON_SALE"));

        BoothMenu updatedMenu = boothMenuRepository.findById(boothMenu.getId()).orElseThrow();
        assertThat(updatedMenu.getName()).isEqualTo("라볶이");
        assertThat(updatedMenu.getPrice()).isEqualTo(4000);
        assertThat(updatedMenu.getImageUrl()).isEqualTo("https://example.com/menu.jpg");
        assertThat(updatedMenu.getStatus()).isEqualTo(BoothMenuStatus.ON_SALE);
    }

    @Test
    void emptyBoothMenuUpdateRequestKeepsExistingFields() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        BoothMenu boothMenu = boothMenuRepository.save(
                new BoothMenu(booth, "떡볶이", 4000, "", "https://example.com/menu.jpg", BoothMenuStatus.SOLD_OUT)
        );

        mockMvc.perform(patch("/api/admin/booths/{boothId}/menus/{menuId}", booth.getId(), boothMenu.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuId").value(boothMenu.getId()))
                .andExpect(jsonPath("$.name").value("떡볶이"))
                .andExpect(jsonPath("$.price").value(4000))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/menu.jpg"))
                .andExpect(jsonPath("$.status").value("SOLD_OUT"));
    }

    @Test
    void invalidBoothMenuUpdateRequestReturnsBadRequest() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        BoothMenu boothMenu = boothMenuRepository.save(
                new BoothMenu(booth, "떡볶이", 4000, "", "https://example.com/menu.jpg", BoothMenuStatus.ON_SALE)
        );
        BoothMenuUpdateRequest request = new BoothMenuUpdateRequest(" ", 0, "not-a-url");

        mockMvc.perform(patch("/api/admin/booths/{boothId}/menus/{menuId}", booth.getId(), boothMenu.getId())
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
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("price")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("imageUrl")));
    }

    @Test
    void updateBoothMenuOnAnotherBoothReturnsNotFound() throws Exception {
        Booth firstBooth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        Booth secondBooth = boothRepository.save(
                new Booth("전자공학과", "음료 판매", "https://example.com/booth-2.jpg", null)
        );
        BoothMenu boothMenu = boothMenuRepository.save(
                new BoothMenu(firstBooth, "떡볶이", 4000, "", "https://example.com/menu.jpg", BoothMenuStatus.ON_SALE)
        );
        BoothMenuUpdateRequest request = new BoothMenuUpdateRequest("라볶이", null, null);

        mockMvc.perform(patch("/api/admin/booths/{boothId}/menus/{menuId}", secondBooth.getId(), boothMenu.getId())
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

    @Test
    void departmentCouncilCanUpdateBoothMenuStatus() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        BoothMenu boothMenu = boothMenuRepository.save(
                new BoothMenu(booth, "떡볶이", 4000, "", "https://example.com/menu.jpg", BoothMenuStatus.ON_SALE)
        );
        BoothMenuStatusUpdateRequest request = new BoothMenuStatusUpdateRequest("SOLD_OUT");

        mockMvc.perform(patch("/api/admin/booths/{boothId}/menus/{menuId}/status", booth.getId(), boothMenu.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuId").value(boothMenu.getId()))
                .andExpect(jsonPath("$.status").value("SOLD_OUT"));

        BoothMenu updatedMenu = boothMenuRepository.findById(boothMenu.getId()).orElseThrow();
        assertThat(updatedMenu.getStatus()).isEqualTo(BoothMenuStatus.SOLD_OUT);
    }

    @Test
    void invalidBoothMenuStatusReturnsValidationError() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        BoothMenu boothMenu = boothMenuRepository.save(
                new BoothMenu(booth, "떡볶이", 4000, "", "https://example.com/menu.jpg", BoothMenuStatus.ON_SALE)
        );
        BoothMenuStatusUpdateRequest request = new BoothMenuStatusUpdateRequest("INVALID");

        mockMvc.perform(patch("/api/admin/booths/{boothId}/menus/{menuId}/status", booth.getId(), boothMenu.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("status")));
    }

    @Test
    void departmentCouncilCanDeleteBoothMenu() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        BoothMenu boothMenu = boothMenuRepository.save(
                new BoothMenu(booth, "떡볶이", 4000, "", "https://example.com/menu.jpg", BoothMenuStatus.ON_SALE)
        );

        mockMvc.perform(delete("/api/admin/booths/{boothId}/menus/{menuId}", booth.getId(), boothMenu.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN))
                .andExpect(status().isNoContent());

        assertThat(boothMenuRepository.findById(boothMenu.getId())).isEmpty();
    }

    @Test
    void deleteBoothMenuOnAnotherBoothReturnsNotFound() throws Exception {
        Booth firstBooth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        Booth secondBooth = boothRepository.save(
                new Booth("전자공학과", "음료 판매", "https://example.com/booth-2.jpg", null)
        );
        BoothMenu boothMenu = boothMenuRepository.save(
                new BoothMenu(firstBooth, "떡볶이", 4000, "", "https://example.com/menu.jpg", BoothMenuStatus.ON_SALE)
        );

        mockMvc.perform(delete("/api/admin/booths/{boothId}/menus/{menuId}", secondBooth.getId(), boothMenu.getId())
                        .secure(true)
                        .with(user("department-admin").roles("DEPARTMENT_COUNCIL"))
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", TEST_CSRF_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void publicBoothMenuApiKeepsKoreanStatusLabel() throws Exception {
        Booth booth = boothRepository.save(
                new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", null)
        );
        BoothMenu boothMenu = boothMenuRepository.save(
                new BoothMenu(booth, "떡볶이", 4000, "", "https://example.com/menu.jpg", BoothMenuStatus.SOLD_OUT)
        );

        mockMvc.perform(get("/api/booths/{id}/menu", booth.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].menuId").value(boothMenu.getId()))
                .andExpect(jsonPath("$[0].name").value("떡볶이"))
                .andExpect(jsonPath("$[0].price").value(4000))
                .andExpect(jsonPath("$[0].imageUrl").value("https://example.com/menu.jpg"))
                .andExpect(jsonPath("$[0].status").value("품절"));
    }

    private Cookie csrfCookie() {
        Cookie cookie = new Cookie("XSRF-TOKEN", TEST_CSRF_TOKEN);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }
}
