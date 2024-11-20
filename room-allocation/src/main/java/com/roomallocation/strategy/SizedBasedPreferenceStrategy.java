package com.roomallocation.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.roomallocation.model.Course;
import com.roomallocation.model.Room;
import com.roomallocation.model.RoomType;

public class SizedBasedPreferenceStrategy extends PreferenceGenerationStrategy {
    private final int numPreferences;
    private int extraCapacity = 10; // Extra capacity to account for growth
    private final Map<RoomType, Integer> medianCapacities;

    public int getExtraCapacity() {
        return extraCapacity;
    }

    public void setExtraCapacity(int extraCapacity) {
        this.extraCapacity = extraCapacity;
    }

    public SizedBasedPreferenceStrategy(int numPreferences, List<Room> rooms) {
        super(numPreferences, "sized");
        this.numPreferences = numPreferences;
        this.medianCapacities = calculateMedianCapacities(rooms);
    }

    private Map<RoomType, Integer> calculateMedianCapacities(List<Room> rooms) {
        Map<RoomType, List<Integer>> capacities = new EnumMap<>(RoomType.class);
        for (RoomType type : RoomType.values()) {
            capacities.put(type, new ArrayList<>());
        }

        // Collect all capacities by room type
        for (Room room : rooms) {
            capacities.get(room.getType()).add(room.getCapacity());
        }

        // Calculate medians
        Map<RoomType, Integer> medians = new EnumMap<>(RoomType.class);
        for (Map.Entry<RoomType, List<Integer>> entry : capacities.entrySet()) {
            List<Integer> roomCapacities = entry.getValue();
            if (!roomCapacities.isEmpty()) {
                // Sort the capacities
                List<Integer> sorted = new ArrayList<>(roomCapacities);
                Collections.sort(sorted);
                
                // Calculate median
                int size = sorted.size();
                int median;
                if (size % 2 == 0) {
                    // If even number of elements, average the two middle values
                    median = (sorted.get((size - 1) / 2) + sorted.get(size / 2)) / 2;
                } else {
                    // If odd number of elements, take the middle value
                    median = sorted.get(size / 2);
                }
                medians.put(entry.getKey(), median);
            } else {
                medians.put(entry.getKey(), 0);
            }
        }

        return medians;
    }

    public List<RoomType> generatePreferences(Course course) {
        // Sort room types by how well they match the course size
        List<RoomType> sortedTypes = new ArrayList<>(availableRoomTypes);
        int courseSize = course.getCohortSize()+extraCapacity;
        
        sortedTypes.sort((type1, type2) -> {
            int diff1 = Math.abs(medianCapacities.get(type1) - courseSize);
            int diff2 = Math.abs(medianCapacities.get(type2) - courseSize);
            return Integer.compare(diff1, diff2);
        });

        // Return top 3 best fitting room types
        return sortedTypes.subList(0, Math.min(numPreferences, sortedTypes.size()));
    }
}
