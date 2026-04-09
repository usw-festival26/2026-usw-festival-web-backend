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
class EventEntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void eventCanBePersisted() {
        Event event = new Event("보물찾기", "캠퍼스 전역에서 진행하는 이벤트", "https://example.com/event.jpg", EventStatus.ONGOING);

        entityManager.persist(event);
        entityManager.flush();
        entityManager.clear();

        Event savedEvent = entityManager.find(Event.class, event.getId());

        assertThat(savedEvent).isNotNull();
        assertThat(savedEvent.getTitle()).isEqualTo("보물찾기");
        assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.ONGOING);
    }

    @Test
    void eventStatusIsStoredAsString() {
        Event event = new Event("경품 추첨", "축제 종료 직전 추첨 이벤트", null, EventStatus.COMPLETED);

        entityManager.persist(event);
        entityManager.flush();

        String eventStatus = jdbcTemplate.queryForObject(
                "select status from events where id = ?",
                String.class,
                event.getId()
        );

        assertThat(eventStatus).isEqualTo("COMPLETED");
    }

    @Test
    void eventRejectsNullStatus() {
        assertThatThrownBy(() -> new Event("보물찾기", "캠퍼스 이벤트", null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("status must not be null");
    }
}
