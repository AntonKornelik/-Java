package ru.renthome.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import ru.renthome.entity.Booking;

public class BookingRepository {
    private final Storage storage;

    public BookingRepository(Storage storage) {
        this.storage = storage;
    }

    public long nextId() {
        return storage.state().nextBookingId++;
    }

    public Booking save(Booking booking) {
        storage.state().bookings.add(booking);
        storage.save();
        return booking;
    }

    public Optional<Booking> findById(long id) {
        return storage.state().bookings.stream().filter(booking -> booking.getId() == id).findFirst();
    }

    public List<Booking> findAll() {
        return storage.state().bookings.stream()
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                .toList();
    }

    public List<Booking> findByTenant(long tenantId) {
        return findAll().stream().filter(booking -> booking.getTenantId() == tenantId).toList();
    }
}
