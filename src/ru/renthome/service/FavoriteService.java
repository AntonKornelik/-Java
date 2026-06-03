package ru.renthome.service;

import java.util.List;
import ru.renthome.entity.Listing;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;
import ru.renthome.repository.FavoriteRepository;
import ru.renthome.repository.ListingRepository;
import ru.renthome.repository.Storage;

public class FavoriteService {
    private final FavoriteRepository favorites;
    private final ListingRepository listings;

    public FavoriteService(FavoriteRepository favorites, ListingRepository listings, Storage storage) {
        this.favorites = favorites;
        this.listings = listings;
    }

    public void toggle(User user, long listingId) {
        if (user == null || user.getRole() != Role.TENANT) {
            throw new IllegalArgumentException("Избранное доступно арендаторам");
        }
        listings.findById(listingId).orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        favorites.toggle(user.getId(), listingId);
    }

    public boolean contains(User user, long listingId) {
        return user != null && favorites.exists(user.getId(), listingId);
    }

    public List<Listing> forUser(User user) {
        return favorites.findByUser(user.getId()).stream()
                .map(favorite -> listings.findById(favorite.getListingId()).orElse(null))
                .filter(listing -> listing != null)
                .toList();
    }
}
