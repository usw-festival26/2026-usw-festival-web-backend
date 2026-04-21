package com.usw.festival.entity;

import java.util.Arrays;

public enum LostItemCategory {
    ELECTRONICS("전자기기"),
    WALLET_CARD("지갑/카드"),
    CLOTHING_BAG("의류/가방"),
    OTHER("기타");

    private final String label;

    LostItemCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static LostItemCategory from(String value) {
        return Arrays.stream(values())
                .filter(category -> category.name().equals(value) || category.label.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("허용되지 않는 분실물 카테고리입니다. value=" + value));
    }
}
