package com.usw.festival.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "booths")
public class Booth extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(length = 2048)
    private String imageUrl;

    @Column(length = 100)
    private String notice;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private College college;

    protected Booth() {
    }

    public Booth(String name, String description, String imageUrl, String notice, College college) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.notice = notice;
        this.college = college;
    }

    public void update(String name, String description, String imageUrl, String notice, College college) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (notice != null) {
            this.notice = notice;
        }
        if (college != null) {
            this.college = college;
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getNotice() {
        return notice;
    }

    public College getCollege() {
        return college;
    }
}
