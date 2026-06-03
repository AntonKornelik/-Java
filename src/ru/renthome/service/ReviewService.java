package ru.renthome.service;

import java.util.List;
import ru.renthome.entity.Review;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;
import ru.renthome.repository.ListingRepository;
import ru.renthome.repository.ReviewRepository;
import ru.renthome.repository.Storage;
import ru.renthome.repository.UserRepository;

public class ReviewService {
    private final ReviewRepository reviews;
    private final ListingRepository listings;
    private final UserRepository users;

    public ReviewService(ReviewRepository reviews, ListingRepository listings, UserRepository users, Storage storage) {
        this.reviews = reviews;
        this.listings = listings;
        this.users = users;
    }

    public Review add(User user, long listingId, int rating, String text) {
        if (user == null || user.getRole() != Role.TENANT) {
            throw new IllegalArgumentException("Отзывы могут оставлять арендаторы");
        }
        listings.findById(listingId).orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        if (rating < 1 || rating > 5 || text.isBlank()) {
            throw new IllegalArgumentException("Укажите оценку от 1 до 5 и текст отзыва");
        }
        return reviews.save(new Review(reviews.nextId(), listingId, user.getId(), rating, text.trim()));
    }

    public List<Review> forListing(long listingId) {
        return reviews.findByListing(listingId);
    }

    public String authorName(Review review) {
        return users.findById(review.getAuthorId()).map(User::getName).orElse("Гость");
    }
}
