package ru.renthome.repository;

import java.util.List;
import ru.renthome.entity.Favorite;

public class FavoriteRepository {
    private final Storage storage;

    public FavoriteRepository(Storage storage) {
        this.storage = storage;
    }

    public void toggle(long userId, long listingId) {
        boolean removed = storage.state().favorites.removeIf(favorite ->
                favorite.getUserId() == userId && favorite.getListingId() == listingId);
        if (!removed) {
            storage.state().favorites.add(new Favorite(userId, listingId));
        }
        storage.save();
    }

    public boolean exists(long userId, long listingId) {
        return storage.state().favorites.stream()
                .anyMatch(favorite -> favorite.getUserId() == userId && favorite.getListingId() == listingId);
    }

    public List<Favorite> findByUser(long userId) {
        return storage.state().favorites.stream()
                .filter(favorite -> favorite.getUserId() == userId)
                .toList();
    }
}
