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
@Table(name = "lost_items")
public class LostItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LostItemStatus status;

    @Column(length = 2048)
    private String imageUrl;

    protected LostItem() {
    }

    public LostItem(String name, String description, LostItemStatus status, String imageUrl) {
        this.name = name;
        this.description = description;
        this.status = status == null ? LostItemStatus.STORED : status;
        this.imageUrl = imageUrl;
    }

    public void changeStatus(LostItemStatus status) {
        if (status != null) {
            this.status = status;
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

    public LostItemStatus getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
