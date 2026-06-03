package ru.renthome.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import ru.renthome.entity.Listing;
import ru.renthome.entity.ListingStatus;

public class ListingRepository {
    private final Storage storage;

    public ListingRepository(Storage storage) {
        this.storage = storage;
    }

    public long nextId() {
        return storage.state().nextListingId++;
    }

    public Listing save(Listing listing) {
        storage.state().listings.add(listing);
        storage.save();
        return listing;
    }

    public Optional<Listing> findById(long id) {
        return storage.state().listings.stream().filter(listing -> listing.getId() == id).findFirst();
    }

    public List<Listing> findAll() {
        return storage.state().listings.stream()
                .sorted(Comparator.comparing(Listing::getCreatedAt).reversed())
                .toList();
    }

    public List<Listing> findActive() {
        return findAll().stream().filter(listing -> listing.getStatus() == ListingStatus.ACTIVE).toList();
    }

    public List<Listing> findByOwner(long ownerId) {
        return findAll().stream().filter(listing -> listing.getOwnerId() == ownerId).toList();
    }
}
