package ru.renthome.entity;

import java.io.Serializable;

public enum ListingStatus implements Serializable {
    ACTIVE("Активно"),
    HIDDEN("Скрыто"),
    DELETED("Удалено");

    private final String title;

    ListingStatus(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
