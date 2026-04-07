package com.usw.festival.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class NoticeEntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void noticeCanBePersisted() {
        Notice notice = new Notice("축제 운영 안내", "공지 본문", true);

        entityManager.persist(notice);
        entityManager.flush();
        entityManager.clear();

        Notice savedNotice = entityManager.find(Notice.class, notice.getId());

        assertThat(savedNotice).isNotNull();
        assertThat(savedNotice.getTitle()).isEqualTo("축제 운영 안내");
        assertThat(savedNotice.isPinned()).isTrue();
    }

    @Test
    void createdAtAndUpdatedAtAreManagedAutomatically() {
        Notice notice = new Notice("운영 안내", "초기 본문", false);
        entityManager.persist(notice);
        entityManager.flush();

        LocalDateTime createdAt = notice.getCreatedAt();
        LocalDateTime firstUpdatedAt = notice.getUpdatedAt();

        notice.update("운영 시간 변경", null, null);
        entityManager.flush();
        entityManager.clear();

        Notice updatedNotice = entityManager.find(Notice.class, notice.getId());

        assertThat(createdAt).isNotNull();
        assertThat(firstUpdatedAt).isNotNull();
        assertThat(updatedNotice.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updatedNotice.getUpdatedAt()).isAfterOrEqualTo(firstUpdatedAt);
    }

    @Test
    void hardDeleteRemovesRow() {
        Notice notice = new Notice("삭제 테스트", "본문", false);
        entityManager.persist(notice);
        entityManager.flush();

        entityManager.remove(notice);
        entityManager.flush();
        entityManager.clear();

        Notice deletedNotice = entityManager.find(Notice.class, notice.getId());
        Integer rowCount = jdbcTemplate.queryForObject("select count(*) from notices where id = ?", Integer.class, notice.getId());

        assertThat(deletedNotice).isNull();
        assertThat(rowCount).isZero();
    }
}
