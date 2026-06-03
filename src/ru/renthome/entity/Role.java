package ru.renthome.entity;

import java.io.Serializable;

public enum Role implements Serializable {
    TENANT("Арендатор"),
    OWNER("Владелец"),
    ADMIN("Администратор");

    private final String title;

    Role(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
