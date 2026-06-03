package ru.renthome.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking implements Serializable {
    private final long id;
    private final long listingId;
    private final long tenantId;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final int guests;
    private final String comment;
    private BookingStatus status = BookingStatus.PENDING;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Booking(long id, long listingId, long tenantId, LocalDate dateFrom, LocalDate dateTo, int guests, String comment) {
        this.id = id;
        this.listingId = listingId;
        this.tenantId = tenantId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.guests = guests;
        this.comment = comment;
    }

    public long getId() { return id; }
    public long getListingId() { return listingId; }
    public long getTenantId() { return tenantId; }
    public LocalDate getDateFrom() { return dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public int getGuests() { return guests; }
    public String getComment() { return comment; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setStatus(BookingStatus status) { this.status = status; }
}
