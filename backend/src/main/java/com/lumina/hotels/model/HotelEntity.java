package com.lumina.hotels.model;

import java.util.ArrayList;
import java.util.List;

public class HotelEntity {
    private String id;
    private String name;
    private String city;
    private String address;
    private int starRating;
    private double reviewScore;
    private int reviewCount;
    private String reviewLabel;
    private String description;
    private List<String> amenities;
    private double pricePerNightStarting;
    private String featuredTag;
    private String imageUrl;
    private boolean hasGeniusDiscount;
    private List<HotelRoomEntity> rooms;

    public HotelEntity() {
        this.starRating = 5;
        this.reviewScore = 9.4;
        this.reviewCount = 120;
        this.reviewLabel = "Excepcional";
        this.amenities = new ArrayList<>();
        this.hasGeniusDiscount = true;
        this.rooms = new ArrayList<>();
        this.imageUrl = "";
    }

    public HotelEntity(String id, String name, String city, String address, int starRating,
                       double reviewScore, int reviewCount, String reviewLabel, String description,
                       List<String> amenities, double pricePerNightStarting, String featuredTag,
                       String imageUrl, List<HotelRoomEntity> rooms) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.starRating = starRating;
        this.reviewScore = reviewScore;
        this.reviewCount = reviewCount;
        this.reviewLabel = reviewLabel != null ? reviewLabel : "Excepcional";
        this.description = description;
        this.amenities = amenities != null ? amenities : new ArrayList<>();
        this.pricePerNightStarting = pricePerNightStarting;
        this.featuredTag = featuredTag;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.hasGeniusDiscount = true;
        this.rooms = rooms != null ? rooms : new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public int getStarRating() { return starRating; }
    public void setStarRating(int starRating) { this.starRating = starRating; }
    public double getReviewScore() { return reviewScore; }
    public void setReviewScore(double reviewScore) { this.reviewScore = reviewScore; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public String getReviewLabel() { return reviewLabel; }
    public void setReviewLabel(String reviewLabel) { this.reviewLabel = reviewLabel; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }
    public double getPricePerNightStarting() { return pricePerNightStarting; }
    public void setPricePerNightStarting(double pricePerNightStarting) { this.pricePerNightStarting = pricePerNightStarting; }
    public String getFeaturedTag() { return featuredTag; }
    public void setFeaturedTag(String featuredTag) { this.featuredTag = featuredTag; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isHasGeniusDiscount() { return hasGeniusDiscount; }
    public void setHasGeniusDiscount(boolean hasGeniusDiscount) { this.hasGeniusDiscount = hasGeniusDiscount; }
    public List<HotelRoomEntity> getRooms() { return rooms; }
    public void setRooms(List<HotelRoomEntity> rooms) { this.rooms = rooms; }

    public int getTotalAvailableRooms() {
        if (rooms == null) return 0;
        return rooms.stream().mapToInt(HotelRoomEntity::getAvailableRooms).sum();
    }
}
