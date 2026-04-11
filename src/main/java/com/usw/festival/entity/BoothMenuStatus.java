package com.usw.festival.entity;

public enum BoothMenuStatus {
    ON_SALE("판매 중"),
    SOLD_OUT("품절");

    private final String label;

    BoothMenuStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
