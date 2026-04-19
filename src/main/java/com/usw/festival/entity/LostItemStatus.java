package com.usw.festival.entity;

public enum LostItemStatus {
    STORED("보관 중"),
    CLAIMED("수령 완료");

    private final String label;

    LostItemStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
