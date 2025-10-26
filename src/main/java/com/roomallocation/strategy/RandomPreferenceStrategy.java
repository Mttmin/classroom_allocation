package com.roomallocation.strategy;

import java.util.List;

import com.roomallocation.model.Course;
import com.roomallocation.model.RoomType;

public class RandomPreferenceStrategy extends PreferenceGenerationStrategy {
    private final int numPreferences;

    public RandomPreferenceStrategy(int numPreferences) {
        super(numPreferences, "random");
        this.numPreferences = numPreferences;
    }

    @Override
    public List<RoomType> generatePreferences(Course course) {
        List<RoomType> preferences = getShuffledRoomTypes();
        return preferences.subList(0, Math.min(numPreferences, preferences.size()));
    }
}
