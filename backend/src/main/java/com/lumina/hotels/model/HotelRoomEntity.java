package com.lumina.hotels.model;

import java.util.ArrayList;
import java.util.List;

public class HotelRoomEntity {
    private int roomId;
    private String hotelId;
    private String name;
    private String bedConfiguration;
    private int capacity;
    private int sizeM2;
    private double pricePerNight;
    private List<String> amenities;
    private String imageUrl;
    private boolean freeCancellation;
    private boolean breakfastIncluded;
    private int backendHabitacionId;
    private int backendTipoId;
    private int availableRooms;
    private int totalRooms;

    public HotelRoomEntity() {
        this.amenities = new ArrayList<>();
        this.freeCancellation = true;
        this.breakfastIncluded = true;
        this.availableRooms = 5;
        this.totalRooms = 5;
    }

    public HotelRoomEntity(int roomId, String hotelId, String name, String bedConfiguration,
                           int capacity, int sizeM2, double pricePerNight, List<String> amenities,
                           String imageUrl, int availableRooms, int totalRooms) {
        this.roomId = roomId;
        this.hotelId = hotelId;
        this.name = name;
        this.bedConfiguration = bedConfiguration;
        this.capacity = capacity;
        this.sizeM2 = sizeM2;
        this.pricePerNight = pricePerNight;
        this.amenities = amenities != null ? amenities : new ArrayList<>();
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.freeCancellation = true;
        this.breakfastIncluded = true;
        this.backendHabitacionId = roomId;
        this.backendTipoId = 1;
        this.availableRooms = availableRooms;
        this.totalRooms = totalRooms;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBedConfiguration() { return bedConfiguration; }
    public void setBedConfiguration(String bedConfiguration) { this.bedConfiguration = bedConfiguration; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getSizeM2() { return sizeM2; }
    public void setSizeM2(int sizeM2) { this.sizeM2 = sizeM2; }
    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isFreeCancellation() { return freeCancellation; }
    public void setFreeCancellation(boolean freeCancellation) { this.freeCancellation = freeCancellation; }
    public boolean isBreakfastIncluded() { return breakfastIncluded; }
    public void setBreakfastIncluded(boolean breakfastIncluded) { this.breakfastIncluded = breakfastIncluded; }
    public int getBackendHabitacionId() { return backendHabitacionId; }
    public void setBackendHabitacionId(int backendHabitacionId) { this.backendHabitacionId = backendHabitacionId; }
    public int getBackendTipoId() { return backendTipoId; }
    public void setBackendTipoId(int backendTipoId) { this.backendTipoId = backendTipoId; }
    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
}
