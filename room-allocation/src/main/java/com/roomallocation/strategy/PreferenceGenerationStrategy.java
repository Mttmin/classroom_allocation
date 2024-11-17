package com.roomallocation.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.roomallocation.model.Course;
import com.roomallocation.model.RoomType;

public abstract class PreferenceGenerationStrategy {
    protected final List<RoomType> availableRoomTypes;
    protected final Random random;

    public PreferenceGenerationStrategy() {
        this.availableRoomTypes = Arrays.asList(RoomType.values());
        this.random = new Random();
    }

    /**
     * Generate room type preferences for a given course
     * @param course The course to generate preferences for
     * @return List of room type preferences
     */
    public abstract List<RoomType> generatePreferences(Course course);

    /**
     * Helper method to shuffle a copy of the available room types
     * @return Shuffled list of room types
     */
    protected List<RoomType> getShuffledRoomTypes() {
        List<RoomType> shuffled = new ArrayList<>(availableRoomTypes);
        Collections.shuffle(shuffled, random);
        return shuffled;
    }
}
