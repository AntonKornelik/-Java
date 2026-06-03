package ru.renthome.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Review implements Serializable {
    private final long id;
    private final long listingId;
    private final long authorId;
    private final int rating;
    private final String text;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Review(long id, long listingId, long authorId, int rating, String text) {
        this.id = id;
        this.listingId = listingId;
        this.authorId = authorId;
        this.rating = rating;
        this.text = text;
    }

    public long getId() { return id; }
    public long getListingId() { return listingId; }
    public long getAuthorId() { return authorId; }
    public int getRating() { return rating; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
