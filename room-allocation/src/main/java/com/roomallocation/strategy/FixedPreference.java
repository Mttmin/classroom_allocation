package com.roomallocation.strategy;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import com.roomallocation.model.Course;
import com.roomallocation.model.RoomType;

public class FixedPreference extends PreferenceGenerationStrategy {
    private final List<RoomType> preferences;
    private final int numPreferences;

    public FixedPreference(int numPreferences) {
        super(numPreferences, "fixed");
        this.numPreferences = numPreferences;
        this.preferences = new ArrayList<>(Arrays.asList(
            RoomType.NOUVEAUX_AMPHIS,
            RoomType.SALLES_100,
            RoomType.COULOIR_SCOLARITE,
            RoomType.COULOIR_LABOS,
            RoomType.SALLES_INFO,
            RoomType.SALLES_LANGUES,
            RoomType.GRANDS_AMPHIS,
            RoomType.COULOIR_VANNEAU,
            RoomType.AMPHIS_80_100,
            RoomType.AMPHI_COULOIR_BINETS
        ));
    }

    @Override
    public List<RoomType> generatePreferences(Course course) {
        return preferences.subList(0, numPreferences);
    }
}