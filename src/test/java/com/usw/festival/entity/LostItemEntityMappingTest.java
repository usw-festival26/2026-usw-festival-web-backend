package com.usw.festival.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class LostItemEntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void lostItemCanBePersistedWithDefaultStatusAndCategory() {
        LostItem lostItem = new LostItem(
                "검은색 지갑",
                "학생증과 체크카드가 들어 있음",
                null,
                null,
                "https://example.com/lost-item.jpg"
        );

        entityManager.persist(lostItem);
        entityManager.flush();
        entityManager.clear();

        LostItem savedLostItem = entityManager.find(LostItem.class, lostItem.getId());

        assertThat(savedLostItem).isNotNull();
        assertThat(savedLostItem.getStatus()).isEqualTo(LostItemStatus.STORED);
        assertThat(savedLostItem.getCategory()).isEqualTo(LostItemCategory.OTHER);
    }

    @Test
    void lostItemStatusIsStoredAsString() {
        LostItem lostItem = new LostItem(
                "검은색 지갑",
                "분실물 설명",
                LostItemStatus.CLAIMED,
                LostItemCategory.OTHER,
                null
        );

        entityManager.persist(lostItem);
        entityManager.flush();

        String lostItemStatus = jdbcTemplate.queryForObject(
                "select status from lost_items where id = ?",
                String.class,
                lostItem.getId()
        );

        assertThat(lostItemStatus).isEqualTo("CLAIMED");
    }

    @Test
    void lostItemCategoryIsStoredAsString() {
        LostItem lostItem = new LostItem(
                "무선 이어폰",
                "검은색 케이스 포함",
                LostItemStatus.STORED,
                LostItemCategory.ELECTRONICS,
                null
        );

        entityManager.persist(lostItem);
        entityManager.flush();

        String lostItemCategory = jdbcTemplate.queryForObject(
                "select category from lost_items where id = ?",
                String.class,
                lostItem.getId()
        );

        assertThat(lostItemCategory).isEqualTo("ELECTRONICS");
    }

    @Test
    void legacyNullCategoryIsExposedAsOther() {
        LostItem lostItem = new LostItem(
                "검은색 지갑",
                "분실물 설명",
                LostItemStatus.STORED,
                LostItemCategory.WALLET_CARD,
                null
        );

        entityManager.persist(lostItem);
        entityManager.flush();

        jdbcTemplate.update("update lost_items set category = null where id = ?", lostItem.getId());
        entityManager.clear();

        LostItem savedLostItem = entityManager.find(LostItem.class, lostItem.getId());

        assertThat(savedLostItem.getCategory()).isEqualTo(LostItemCategory.OTHER);
    }
}
