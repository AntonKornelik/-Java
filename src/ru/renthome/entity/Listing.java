package ru.renthome.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Listing implements Serializable {
    private final long id;
    private final long ownerId;
    private String title;
    private String city;
    private String address;
    private String type;
    private int price;
    private int area;
    private int rooms;
    private String description;
    private String imageUrl;
    private ListingStatus status = ListingStatus.ACTIVE;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Listing(long id, long ownerId, String title, String city, String address, String type, int price,
                   int area, int rooms, String description, String imageUrl) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.city = city;
        this.address = address;
        this.type = type;
        this.price = price;
        this.area = area;
        this.rooms = rooms;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public long getId() { return id; }
    public long getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getType() { return type; }
    public int getPrice() { return price; }
    public int getArea() { return area; }
    public int getRooms() { return rooms; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public ListingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTitle(String title) { this.title = title; }
    public void setCity(String city) { this.city = city; }
    public void setAddress(String address) { this.address = address; }
    public void setType(String type) { this.type = type; }
    public void setPrice(int price) { this.price = price; }
    public void setArea(int area) { this.area = area; }
    public void setRooms(int rooms) { this.rooms = rooms; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setStatus(ListingStatus status) { this.status = status; }
}
