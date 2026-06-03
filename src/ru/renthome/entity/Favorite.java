package ru.renthome.entity;

import java.io.Serializable;

public class Favorite implements Serializable {
    private final long userId;
    private final long listingId;

    public Favorite(long userId, long listingId) {
        this.userId = userId;
        this.listingId = listingId;
    }

    public long getUserId() { return userId; }
    public long getListingId() { return listingId; }
}
