package ru.renthome.repository;

import java.util.Comparator;
import java.util.List;
import ru.renthome.entity.Review;

public class ReviewRepository {
    private final Storage storage;

    public ReviewRepository(Storage storage) {
        this.storage = storage;
    }

    public long nextId() {
        return storage.state().nextReviewId++;
    }

    public Review save(Review review) {
        storage.state().reviews.add(review);
        storage.save();
        return review;
    }

    public List<Review> findByListing(long listingId) {
        return storage.state().reviews.stream()
                .filter(review -> review.getListingId() == listingId)
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .toList();
    }
}
