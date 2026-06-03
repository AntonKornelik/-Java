package ru.renthome.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import ru.renthome.entity.Booking;
import ru.renthome.entity.Favorite;
import ru.renthome.entity.Listing;
import ru.renthome.entity.Review;
import ru.renthome.entity.User;

public class DataState implements Serializable {
    public long nextUserId = 1;
    public long nextListingId = 1;
    public long nextBookingId = 1;
    public long nextReviewId = 1;
    public final List<User> users = new ArrayList<>();
    public final List<Listing> listings = new ArrayList<>();
    public final List<Booking> bookings = new ArrayList<>();
    public final List<Favorite> favorites = new ArrayList<>();
    public final List<Review> reviews = new ArrayList<>();
}
