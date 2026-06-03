package ru.renthome.service;

import java.time.LocalDate;
import java.util.List;
import ru.renthome.entity.Booking;
import ru.renthome.entity.BookingStatus;
import ru.renthome.entity.Listing;
import ru.renthome.entity.ListingStatus;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;
import ru.renthome.repository.BookingRepository;
import ru.renthome.repository.ListingRepository;
import ru.renthome.repository.Storage;

public class BookingService {
    private final BookingRepository bookings;
    private final ListingRepository listings;
    private final Storage storage;

    public BookingService(BookingRepository bookings, ListingRepository listings, Storage storage) {
        this.bookings = bookings;
        this.listings = listings;
        this.storage = storage;
    }

    public Booking create(User tenant, long listingId, LocalDate from, LocalDate to, int guests, String comment) {
        if (tenant == null || tenant.getRole() != Role.TENANT) {
            throw new IllegalArgumentException("Заявку может отправить только арендатор");
        }
        Listing listing = listings.findById(listingId).orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalArgumentException("Объявление сейчас недоступно");
        }
        if (from == null || to == null || !to.isAfter(from)) {
            throw new IllegalArgumentException("Дата выезда должна быть позже даты заезда");
        }
        if (guests <= 0) {
            throw new IllegalArgumentException("Количество жильцов должно быть положительным");
        }
        return bookings.save(new Booking(bookings.nextId(), listingId, tenant.getId(), from, to, guests, comment.trim()));
    }

    public void changeStatus(User owner, long bookingId, BookingStatus status) {
        Booking booking = bookings.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        Listing listing = listings.findById(booking.getListingId()).orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        if (owner.getRole() != Role.ADMIN && listing.getOwnerId() != owner.getId()) {
            throw new IllegalArgumentException("Статус заявки может менять владелец жилья");
        }
        booking.setStatus(status);
        storage.save();
    }

    public List<Booking> forUser(User user) {
        if (user.getRole() == Role.ADMIN) {
            return bookings.findAll();
        }
        if (user.getRole() == Role.TENANT) {
            return bookings.findByTenant(user.getId());
        }
        return bookings.findAll().stream()
                .filter(booking -> listings.findById(booking.getListingId())
                        .map(listing -> listing.getOwnerId() == user.getId())
                        .orElse(false))
                .toList();
    }
}
