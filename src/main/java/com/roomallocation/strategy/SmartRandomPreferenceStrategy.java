package com.roomallocation.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.roomallocation.model.Course;
import com.roomallocation.model.Room;
import com.roomallocation.model.RoomType;

public class SmartRandomPreferenceStrategy extends PreferenceGenerationStrategy {
    private final Map<RoomType, Integer> maxCapacities;

    public SmartRandomPreferenceStrategy(int numPreferences, List<Room> rooms) {
        super(numPreferences, "smart_random");
        this.maxCapacities = new EnumMap<>(RoomType.class);

        // Initialize all room types with capacity 0
        for (RoomType type : RoomType.values()) {
            maxCapacities.put(type, 0);
        }

        // Find maximum capacity for each room type that exists
        for (Room room : rooms) {
            maxCapacities.merge(room.getType(), room.getCapacity(), Math::max);
        }
    }

    @Override
    public List<RoomType> generatePreferences(Course course) {
        // Filter room types that could potentially fit the course
        List<RoomType> suitableTypes = new ArrayList<>();
        for (RoomType type : availableRoomTypes) {
            Integer maxCapacity = maxCapacities.get(type);
            if (maxCapacity != null && maxCapacity > 0 && course.getCohortSize() <= maxCapacity) {
                suitableTypes.add(type);
            }
        }

        // If no suitable types found, return all types as fallback
        if (suitableTypes.isEmpty()) {
            suitableTypes = new ArrayList<>(availableRoomTypes);
        }

        // Randomly select from suitable types
        List<RoomType> shuffled = new ArrayList<>(suitableTypes);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, Math.min(numPreferences, shuffled.size()));
    }
}
