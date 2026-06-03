package ru.renthome.service;

import java.util.List;
import ru.renthome.entity.Listing;
import ru.renthome.entity.ListingStatus;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;
import ru.renthome.repository.ListingRepository;
import ru.renthome.repository.Storage;
import ru.renthome.repository.UserRepository;

public class ListingService {
    private final ListingRepository listings;
    private final UserRepository users;
    private final Storage storage;

    public ListingService(ListingRepository listings, UserRepository users, Storage storage) {
        this.listings = listings;
        this.users = users;
        this.storage = storage;
    }

    public Listing create(User owner, String title, String city, String address, String type, int price, int area,
                          int rooms, String description, String imageUrl) {
        requireOwner(owner);
        validate(title, city, price, area, rooms);
        Listing listing = new Listing(listings.nextId(), owner.getId(), title.trim(), city.trim(), address.trim(),
                type.trim(), price, area, rooms, description.trim(), normalizeImage(imageUrl));
        return listings.save(listing);
    }

    public void update(User user, long id, String title, String city, String address, String type, int price, int area,
                       int rooms, String description, String imageUrl) {
        Listing listing = require(id);
        if (user.getRole() != Role.ADMIN && listing.getOwnerId() != user.getId()) {
            throw new IllegalArgumentException("Редактировать объявление может только владелец");
        }
        validate(title, city, price, area, rooms);
        listing.setTitle(title.trim());
        listing.setCity(city.trim());
        listing.setAddress(address.trim());
        listing.setType(type.trim());
        listing.setPrice(price);
        listing.setArea(area);
        listing.setRooms(rooms);
        listing.setDescription(description.trim());
        listing.setImageUrl(normalizeImage(imageUrl));
        storage.save();
    }

    public Listing require(long id) {
        return listings.findById(id).orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
    }

    public List<Listing> search(String city, Integer maxPrice, Integer rooms, String type, boolean includeHidden) {
        return (includeHidden ? listings.findAll() : listings.findActive()).stream()
                .filter(listing -> city == null || city.isBlank()
                        || listing.getCity().toLowerCase().contains(city.toLowerCase().trim()))
                .filter(listing -> maxPrice == null || listing.getPrice() <= maxPrice)
                .filter(listing -> rooms == null || rooms == 0 || listing.getRooms() == rooms)
                .filter(listing -> type == null || type.isBlank()
                        || listing.getType().toLowerCase().contains(type.toLowerCase().trim()))
                .toList();
    }

    public List<Listing> byOwner(long ownerId) {
        return listings.findByOwner(ownerId);
    }

    public String ownerName(Listing listing) {
        return users.findById(listing.getOwnerId()).map(User::getName).orElse("Неизвестный владелец");
    }

    public void setStatus(User user, long listingId, ListingStatus status) {
        Listing listing = require(listingId);
        if (user.getRole() != Role.ADMIN && listing.getOwnerId() != user.getId()) {
            throw new IllegalArgumentException("Нет прав на изменение статуса");
        }
        listing.setStatus(status);
        storage.save();
    }

    private void requireOwner(User user) {
        if (user == null || user.getRole() != Role.OWNER) {
            throw new IllegalArgumentException("Создавать объявления может только владелец");
        }
    }

    private void validate(String title, String city, int price, int area, int rooms) {
        if (title.isBlank() || city.isBlank()) {
            throw new IllegalArgumentException("Название и город обязательны");
        }
        if (price <= 0 || area <= 0 || rooms <= 0) {
            throw new IllegalArgumentException("Цена, площадь и комнаты должны быть положительными");
        }
    }

    private String normalizeImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return "https://images.unsplash.com/photo-1560448204-603b3fc33ddc?auto=format&fit=crop&w=1200&q=80";
        }
        return imageUrl.trim();
    }
}
