package com.usw.festival.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AdminAccountEntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void adminAccountCanBePersisted() {
        AdminAccount adminAccount = new AdminAccount("council-admin", "hashed-password", AdminRole.STUDENT_COUNCIL);

        entityManager.persist(adminAccount);
        entityManager.flush();
        entityManager.clear();

        AdminAccount savedAdminAccount = entityManager.find(AdminAccount.class, adminAccount.getId());

        assertThat(savedAdminAccount).isNotNull();
        assertThat(savedAdminAccount.getLoginId()).isEqualTo("council-admin");
        assertThat(savedAdminAccount.getRole()).isEqualTo(AdminRole.STUDENT_COUNCIL);
    }

    @Test
    void adminRoleIsStoredAsString() {
        AdminAccount adminAccount = new AdminAccount("dept-admin", "hashed-password", AdminRole.DEPARTMENT_COUNCIL);

        entityManager.persist(adminAccount);
        entityManager.flush();

        String role = jdbcTemplate.queryForObject(
                "select role from admin_accounts where id = ?",
                String.class,
                adminAccount.getId()
        );

        assertThat(role).isEqualTo("DEPARTMENT_COUNCIL");
    }

    @Test
    void loginIdMustBeUnique() {
        assertThatThrownBy(() -> {
            entityManager.persist(new AdminAccount("duplicate-admin", "hashed-password-1", AdminRole.STUDENT_COUNCIL));
            entityManager.persist(new AdminAccount("duplicate-admin", "hashed-password-2", AdminRole.DEPARTMENT_COUNCIL));
            entityManager.flush();
        })
                .isInstanceOfAny(
                        DataIntegrityViolationException.class,
                        PersistenceException.class,
                        ConstraintViolationException.class
                );
    }

    @Test
    void adminAccountRejectsNullRole() {
        assertThatThrownBy(() -> new AdminAccount("council-admin", "hashed-password", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("role must not be null");
    }
}
