package com.lumina.hotels.model;

import java.util.ArrayList;
import java.util.List;

public class AtraccionEntity {
    private String id;
    private String title;
    private String city;
    private String category;
    private double rating;
    private int reviewCount;
    private String duration;
    private double pricePerPerson;
    private String description;
    private String imageUrl;
    private List<String> highlights;
    private List<String> inclusions;
    private String cancellationPolicy;
    private int minibusCapacity;
    private int availableSpots;

    public AtraccionEntity() {
        this.rating = 4.9;
        this.reviewCount = 100;
        this.cancellationPolicy = "Cancelación gratuita hasta 24 horas antes";
        this.minibusCapacity = 14;
        this.availableSpots = 14;
        this.highlights = new ArrayList<>();
        this.inclusions = new ArrayList<>();
        this.imageUrl = "";
    }

    public AtraccionEntity(String id, String title, String city, String category,
                           double rating, int reviewCount, String duration, double pricePerPerson,
                           String description, String imageUrl, List<String> highlights,
                           List<String> inclusions, int minibusCapacity, int availableSpots) {
        this.id = id;
        this.title = title;
        this.city = city;
        this.category = category;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.duration = duration;
        this.pricePerPerson = pricePerPerson;
        this.description = description;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.highlights = highlights != null ? highlights : new ArrayList<>();
        this.inclusions = inclusions != null ? inclusions : new ArrayList<>();
        this.cancellationPolicy = "Cancelación gratuita hasta 24 horas antes";
        this.minibusCapacity = minibusCapacity > 0 ? minibusCapacity : 14;
        this.availableSpots = availableSpots >= 0 ? availableSpots : this.minibusCapacity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public double getPricePerPerson() { return pricePerPerson; }
    public void setPricePerPerson(double pricePerPerson) { this.pricePerPerson = pricePerPerson; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<String> getHighlights() { return highlights; }
    public void setHighlights(List<String> highlights) { this.highlights = highlights; }
    public List<String> getInclusions() { return inclusions; }
    public void setInclusions(List<String> inclusions) { this.inclusions = inclusions; }
    public String getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(String cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }
    public int getMinibusCapacity() { return minibusCapacity; }
    public void setMinibusCapacity(int minibusCapacity) { this.minibusCapacity = minibusCapacity; }
    public int getAvailableSpots() { return availableSpots; }
    public void setAvailableSpots(int availableSpots) { this.availableSpots = availableSpots; }
}
