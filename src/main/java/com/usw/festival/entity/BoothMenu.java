package com.usw.festival.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "booth_menus")
public class BoothMenu extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booth_id", nullable = false)
    private Booth booth;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 2048)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoothMenuStatus status;

    protected BoothMenu() {
    }

    public BoothMenu(Booth booth, String name, Integer price, String description, String imageUrl, BoothMenuStatus status) {
        this.booth = booth;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public void update(String name, Integer price, String description, String imageUrl, BoothMenuStatus status) {
        if (name != null) {
            this.name = name;
        }
        if (price != null) {
            this.price = price;
        }
        if (description != null) {
            this.description = description;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (status != null) {
            this.status = status;
        }
    }

    public Long getId() {
        return id;
    }

    public Booth getBooth() {
        return booth;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BoothMenuStatus getStatus() {
        return status;
    }
}
