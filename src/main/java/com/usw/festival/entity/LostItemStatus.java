package com.usw.festival.entity;

import java.util.Arrays;

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

    public static LostItemStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equals(value) || status.label.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("허용되지 않는 분실물 상태입니다. value=" + value));
    }
}
