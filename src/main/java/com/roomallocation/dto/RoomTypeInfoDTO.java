package com.roomallocation.dto;

import com.roomallocation.model.RoomType;
import java.util.List;

/**
 * Data Transfer Object for room type information
 * Contains aggregated data about all rooms of a specific type
 */
public class RoomTypeInfoDTO {
    private RoomType type;
    private String name;
    private String building;
    private SeatRange seatRange;
    private int roomCount;
    private List<String> amenities;
    private List<String> images;

    public RoomTypeInfoDTO() {
    }

    public RoomTypeInfoDTO(RoomType type, String name, String building,
                          SeatRange seatRange, int roomCount,
                          List<String> amenities, List<String> images) {
        this.type = type;
        this.name = name;
        this.building = building;
        this.seatRange = seatRange;
        this.roomCount = roomCount;
        this.amenities = amenities;
        this.images = images;
    }

    // Getters and Setters
    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public SeatRange getSeatRange() {
        return seatRange;
    }

    public void setSeatRange(SeatRange seatRange) {
        this.seatRange = seatRange;
    }

    public int getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(int roomCount) {
        this.roomCount = roomCount;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    /**
     * Inner class representing the seat capacity range for a room type
     */
    public static class SeatRange {
        private int min;
        private int max;

        public SeatRange() {
        }

        public SeatRange(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public void setMin(int min) {
            this.min = min;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }
    }
}
