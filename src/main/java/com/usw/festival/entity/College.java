package com.usw.festival.entity;

public enum College {
    HUMANITIES("인문사회융합대학"),
    BUSINESS("경영공학대학"),
    LIFE("라이프케어사이언스대학"),
    ICT("지능형SW융합대학"),
    DESIGN("디자인앤아트대학"),
    MUSIC("음악테크놀로지대학"),
    ENGINEERING("혁신공과대학");

    private final String displayName;

    College(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
