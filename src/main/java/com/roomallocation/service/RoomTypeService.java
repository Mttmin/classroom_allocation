package com.roomallocation.service;

import com.roomallocation.dto.RoomTypeInfoDTO;
import com.roomallocation.dto.RoomTypeInfoDTO.SeatRange;
import com.roomallocation.model.Room;
import com.roomallocation.model.RoomType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for aggregating and managing room type information
 */
public class RoomTypeService {

    // In-memory storage for room images (can be replaced with database/file storage)
    private final Map<RoomType, List<String>> roomTypeImages = new HashMap<>();

    // Default amenities for each room type (can be customized)
    private final Map<RoomType, List<String>> defaultAmenities = initializeDefaultAmenities();

    public RoomTypeService() {
        // Initialize with placeholder images
        initializePlaceholderImages();
    }

    /**
     * Aggregate room data by type from a list of rooms
     */
    public List<RoomTypeInfoDTO> aggregateRoomsByType(List<Room> rooms) {
        // Group rooms by type
        Map<RoomType, List<Room>> roomsByType = rooms.stream()
                .collect(Collectors.groupingBy(Room::getType));

        // Create DTOs for each room type
        List<RoomTypeInfoDTO> roomTypeInfos = new ArrayList<>();

        for (Map.Entry<RoomType, List<Room>> entry : roomsByType.entrySet()) {
            RoomType type = entry.getKey();
            List<Room> typeRooms = entry.getValue();

            // Calculate seat range
            SeatRange seatRange = calculateSeatRange(typeRooms);

            // Get amenities and images
            List<String> amenities = defaultAmenities.getOrDefault(type, new ArrayList<>());
            List<String> images = roomTypeImages.getOrDefault(type, new ArrayList<>());

            // Create DTO
            RoomTypeInfoDTO dto = new RoomTypeInfoDTO(
                type,
                type.getDisplayName(),
                "Main", // Default building - can be customized per room type
                seatRange,
                typeRooms.size(),
                amenities,
                images
            );

            roomTypeInfos.add(dto);
        }

        return roomTypeInfos;
    }

    /**
     * Get information for a specific room type
     */
    public RoomTypeInfoDTO getRoomTypeInfo(RoomType type, List<Room> allRooms) {
        List<Room> typeRooms = allRooms.stream()
                .filter(room -> room.getType() == type)
                .collect(Collectors.toList());

        if (typeRooms.isEmpty()) {
            return null;
        }

        SeatRange seatRange = calculateSeatRange(typeRooms);
        List<String> amenities = defaultAmenities.getOrDefault(type, new ArrayList<>());
        List<String> images = roomTypeImages.getOrDefault(type, new ArrayList<>());

        return new RoomTypeInfoDTO(
            type,
            type.getDisplayName(),
            "Main",
            seatRange,
            typeRooms.size(),
            amenities,
            images
        );
    }

    /**
     * Calculate the min and max seat capacity for a list of rooms
     */
    private SeatRange calculateSeatRange(List<Room> rooms) {
        IntSummaryStatistics stats = rooms.stream()
                .mapToInt(Room::getCapacity)
                .summaryStatistics();

        return new SeatRange(stats.getMin(), stats.getMax());
    }

    /**
     * Add or update images for a room type
     */
    public void setRoomTypeImages(RoomType type, List<String> imagePaths) {
        roomTypeImages.put(type, new ArrayList<>(imagePaths));
    }

    /**
     * Add a single image to a room type
     */
    public void addRoomTypeImage(RoomType type, String imagePath) {
        if (!roomTypeImages.containsKey(type)) {
            roomTypeImages.put(type, new ArrayList<>());
        }
        roomTypeImages.get(type).add(imagePath);
    }

    /**
     * Initialize default amenities for each room type
     */
    private Map<RoomType, List<String>> initializeDefaultAmenities() {
        Map<RoomType, List<String>> amenities = new HashMap<>();

        amenities.put(RoomType.COULOIR_VANNEAU, Arrays.asList(
            "Standard classroom setup",
            "Whiteboard",
            "Projector",
            "Natural lighting"
        ));

        amenities.put(RoomType.COULOIR_SCOLARITE, Arrays.asList(
            "Medium-sized classrooms",
            "Whiteboard",
            "Projector",
            "Air conditioning"
        ));

        amenities.put(RoomType.COULOIR_LABOS, Arrays.asList(
            "Laboratory setup",
            "Whiteboard",
            "Projector",
            "Lab equipment",
            "Safety equipment"
        ));

        amenities.put(RoomType.SALLES_100, Arrays.asList(
            "Mixed capacity rooms",
            "Whiteboard",
            "Projector",
            "Flexible seating"
        ));

        amenities.put(RoomType.AMPHI_COULOIR_BINETS, Arrays.asList(
            "Amphitheatre seating",
            "Whiteboard",
            "Projector",
            "Sound system",
            "Tiered seating"
        ));

        amenities.put(RoomType.SALLES_INFO, Arrays.asList(
            "Computer workstations",
            "Whiteboard",
            "Projector",
            "High-speed internet",
            "Software licenses"
        ));

        amenities.put(RoomType.SALLES_LANGUES, Arrays.asList(
            "Language lab equipment",
            "Whiteboard",
            "Audio system",
            "Small group setup",
            "Recording capability"
        ));

        amenities.put(RoomType.NOUVEAUX_AMPHIS, Arrays.asList(
            "Modern amphitheatre",
            "Whiteboard",
            "Projector",
            "Sound system",
            "Recording equipment",
            "Climate control"
        ));

        amenities.put(RoomType.GRANDS_AMPHIS, Arrays.asList(
            "Large amphitheatre",
            "Multiple whiteboards",
            "Projector",
            "Advanced sound system",
            "Recording equipment",
            "Stage lighting",
            "Wheelchair accessible"
        ));

        amenities.put(RoomType.AMPHIS_80_100, Arrays.asList(
            "Mid-size amphitheatre",
            "Whiteboard",
            "Projector",
            "Sound system",
            "Tiered seating"
        ));

        return amenities;
    }

    /**
     * Initialize placeholder images for development
     * Replace these with actual image paths when available
     */
    private void initializePlaceholderImages() {
        // Using placeholder.com for development - replace with actual images
        for (RoomType type : RoomType.values()) {
            String typeName = type.name().replace("_", "+");
            roomTypeImages.put(type, Arrays.asList(
                "https://via.placeholder.com/800x600/4A90A4/FFFFFF?text=" + typeName + "+1",
                "https://via.placeholder.com/800x600/5A9AB4/FFFFFF?text=" + typeName + "+2",
                "https://via.placeholder.com/800x600/6AAAC4/FFFFFF?text=" + typeName + "+3"
            ));
        }
    }
}
