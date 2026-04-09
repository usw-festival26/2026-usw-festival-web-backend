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
    void lostItemCanBePersistedWithDefaultStatus() {
        LostItem lostItem = new LostItem(
                "검은색 지갑",
                "학생증과 체크카드가 들어 있음",
                null,
                "https://example.com/lost-item.jpg"
        );

        entityManager.persist(lostItem);
        entityManager.flush();
        entityManager.clear();

        LostItem savedLostItem = entityManager.find(LostItem.class, lostItem.getId());

        assertThat(savedLostItem).isNotNull();
        assertThat(savedLostItem.getStatus()).isEqualTo(LostItemStatus.STORED);
    }

    @Test
    void lostItemStatusIsStoredAsString() {
        LostItem lostItem = new LostItem("검은색 지갑", "분실물 설명", LostItemStatus.CLAIMED, null);

        entityManager.persist(lostItem);
        entityManager.flush();

        String lostItemStatus = jdbcTemplate.queryForObject(
                "select status from lost_items where id = ?",
                String.class,
                lostItem.getId()
        );

        assertThat(lostItemStatus).isEqualTo("CLAIMED");
    }
}
