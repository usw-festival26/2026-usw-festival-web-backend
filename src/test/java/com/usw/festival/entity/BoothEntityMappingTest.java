package com.usw.festival.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class BoothEntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void boothCanBePersisted() {
        Booth booth = new Booth("컴퓨터학부", "분식 판매", "https://example.com/booth.jpg", "재료 소진 시 조기 마감");

        entityManager.persist(booth);
        entityManager.flush();
        entityManager.clear();

        Booth savedBooth = entityManager.find(Booth.class, booth.getId());

        assertThat(savedBooth).isNotNull();
        assertThat(savedBooth.getName()).isEqualTo("컴퓨터학부");
        assertThat(savedBooth.getNotice()).isEqualTo("재료 소진 시 조기 마감");
    }

    @Test
    void boothMenuStoresForeignKeyToBooth() {
        Booth booth = new Booth("컴퓨터학부", "분식 판매", null, null);
        entityManager.persist(booth);

        BoothMenu boothMenu = new BoothMenu(
                booth,
                "떡볶이",
                4000,
                "",
                "https://example.com/menu.jpg",
                BoothMenuStatus.ON_SALE
        );
        entityManager.persist(boothMenu);
        entityManager.flush();
        entityManager.clear();

        BoothMenu savedMenu = entityManager.find(BoothMenu.class, boothMenu.getId());

        assertThat(savedMenu).isNotNull();
        assertThat(savedMenu.getBooth().getId()).isEqualTo(booth.getId());
    }

    @Test
    void boothMenuStatusIsStoredAsString() {
        Booth booth = new Booth("컴퓨터학부", "분식 판매", null, null);
        entityManager.persist(booth);

        BoothMenu boothMenu = new BoothMenu(booth, "떡볶이", 4000, "", null, BoothMenuStatus.SOLD_OUT);
        entityManager.persist(boothMenu);
        entityManager.flush();

        String menuStatus = jdbcTemplate.queryForObject(
                "select status from booth_menus where id = ?",
                String.class,
                boothMenu.getId()
        );

        assertThat(menuStatus).isEqualTo("SOLD_OUT");
    }

    @Test
    void boothMenuRejectsNullStatus() {
        Booth booth = new Booth("컴퓨터학부", "분식 판매", null, null);

        assertThatThrownBy(() -> new BoothMenu(booth, "떡볶이", 4000, "", null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("status must not be null");
    }

    @Test
    void boothUpdateKeepsImageUrlAndNoticeWhenNullIsPassed() {
        Booth booth = new Booth(
                "컴퓨터학부",
                "분식 판매",
                "https://example.com/booth.jpg",
                "재료 소진 시 조기 마감"
        );

        booth.update("소프트웨어학과", null, null, null);

        assertThat(booth.getName()).isEqualTo("소프트웨어학과");
        assertThat(booth.getDescription()).isEqualTo("분식 판매");
        assertThat(booth.getImageUrl()).isEqualTo("https://example.com/booth.jpg");
        assertThat(booth.getNotice()).isEqualTo("재료 소진 시 조기 마감");
    }

    @Test
    void boothMenuUpdateKeepsImageUrlAndStatusWhenNullIsPassed() {
        Booth booth = new Booth("컴퓨터학부", "분식 판매", null, null);
        BoothMenu boothMenu = new BoothMenu(
                booth,
                "떡볶이",
                4000,
                "",
                "https://example.com/menu.jpg",
                BoothMenuStatus.ON_SALE
        );

        boothMenu.update("라볶이", null, null, null, null);

        assertThat(boothMenu.getName()).isEqualTo("라볶이");
        assertThat(boothMenu.getPrice()).isEqualTo(4000);
        assertThat(boothMenu.getImageUrl()).isEqualTo("https://example.com/menu.jpg");
        assertThat(boothMenu.getStatus()).isEqualTo(BoothMenuStatus.ON_SALE);
    }
}
