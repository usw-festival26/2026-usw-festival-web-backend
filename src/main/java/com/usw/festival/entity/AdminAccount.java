package com.usw.festival.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_accounts")
public class AdminAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdminRole role;

    protected AdminAccount() {
    }

    public AdminAccount(String loginId, String passwordHash, AdminRole role) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.role = Objects.requireNonNull(role, "role must not be null");
    }

    public void updateCredentials(String loginId, String passwordHash) {
        if (loginId != null) {
            this.loginId = loginId;
        }
        if (passwordHash != null) {
            this.passwordHash = passwordHash;
        }
    }

    public void changeRole(AdminRole role) {
        if (role != null) {
            this.role = role;
        }
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AdminRole getRole() {
        return role;
    }
}
