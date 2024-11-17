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
    private final int maxPreferences;
    private final Map<RoomType, Integer> maxCapacities;

    public SmartRandomPreferenceStrategy(int maxPreferences, List<Room> rooms) {
        super();
        this.maxPreferences = maxPreferences;
        this.maxCapacities = new EnumMap<>(RoomType.class);
        
        // Find maximum capacity for each room type
        for (Room room : rooms) {
            maxCapacities.merge(room.getType(), room.getCapacity(), Math::max);
        }
    }

    @Override
    public List<RoomType> generatePreferences(Course course) {
        // Filter room types that could potentially fit the course
        List<RoomType> suitableTypes = new ArrayList<>();
        for (RoomType type : availableRoomTypes) {
            if (course.getCohortSize() <= maxCapacities.get(type)) {
                suitableTypes.add(type);
            }
        }
        // Randomly select from suitable types
        List<RoomType> shuffled = new ArrayList<>(suitableTypes);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, Math.min(maxPreferences, shuffled.size()));
    }
}
