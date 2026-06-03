package ru.renthome.entity;

import java.io.Serializable;

public enum BookingStatus implements Serializable {
    PENDING("Ожидает"),
    APPROVED("Подтверждена"),
    REJECTED("Отклонена");

    private final String title;

    BookingStatus(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
