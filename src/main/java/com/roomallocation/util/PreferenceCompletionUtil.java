package com.roomallocation.util;

import com.roomallocation.model.Course;
import com.roomallocation.model.Room;
import com.roomallocation.model.RoomType;
import com.roomallocation.strategy.PreferenceGenerationStrategy;
import com.roomallocation.strategy.SmartRandomPreferenceStrategy;
import com.roomallocation.strategy.RandomPreferenceStrategy;
import com.roomallocation.strategy.SizedBasedPreferenceStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for completing incomplete professor preferences.
 * When professors submit fewer than the recommended number of preferences,
 * this utility uses a configurable strategy to complete them .
 */
public class PreferenceCompletionUtil {

    // Configuration: Recommended number of preferences for optimal allocation
    public static final int RECOMMENDED_NUM_PREFERENCES = 10;

    // Configuration: Strategy to use for completing incomplete preferences
    // Options: "SmartRandom", "Random", "SizeBased"
    // To change strategy, modify this constant
    private static final String PREFERENCE_COMPLETION_STRATEGY = "SmartRandom";

    /**
     * Completes preferences for all courses that have fewer than the recommended number.
     * Preserves existing preferences and adds generated ones to reach the target count.
     *
     * @param courses List of courses to process
     * @param rooms Available rooms for strategy calculation
     */
    public static void completeAllCoursePreferences(List<Course> courses, List<Room> rooms) {
        PreferenceGenerationStrategy strategy = createPreferenceStrategy(rooms);

        for (Course course : courses) {
            completeCoursePreferences(course, strategy);
        }
    }

    /**
     * Completes preferences for a single course if it has fewer than recommended.
     *
     * @param course Course to process
     * @param rooms Available rooms for strategy calculation
     */
    public static void completeSingleCoursePreferences(Course course, List<Room> rooms) {
        PreferenceGenerationStrategy strategy = createPreferenceStrategy(rooms);
        completeCoursePreferences(course, strategy);
    }

    /**
     * Internal method to complete preferences for a single course using a given strategy.
     *
     * @param course Course to process
     * @param strategy Strategy to use for generating preferences
     */
    private static void completeCoursePreferences(Course course, PreferenceGenerationStrategy strategy) {
        List<RoomType> currentPreferences = course.getTypePreferences();

        // Check if preferences need completion
        if (currentPreferences == null || currentPreferences.size() < RECOMMENDED_NUM_PREFERENCES) {
            // Generate complete preferences using the strategy
            List<RoomType> generatedPreferences = strategy.generatePreferences(course);

            // If course has some preferences, preserve them and add generated ones
            if (currentPreferences != null && !currentPreferences.isEmpty()) {
                List<RoomType> completedPreferences = new ArrayList<>(currentPreferences);

                // Add generated preferences that aren't already in the list
                for (RoomType type : generatedPreferences) {
                    if (!completedPreferences.contains(type) &&
                        completedPreferences.size() < RECOMMENDED_NUM_PREFERENCES) {
                        completedPreferences.add(type);
                    }
                }

                course.setTypePreferences(completedPreferences);

                // System.out.println("Completed preferences for course " + course.getName() +
                //                  ": " + currentPreferences.size() + " → " + completedPreferences.size());
            } else {
                // No existing preferences, use all generated preferences
                course.setTypePreferences(generatedPreferences);

                // System.out.println("Generated preferences for course " + course.getName() +
                //                  ": 0 → " + generatedPreferences.size());
            }
        }
    }

    /**
     * Creates the appropriate preference generation strategy based on configuration.
     *
     * @param rooms Available rooms for strategy calculation
     * @return Configured preference generation strategy
     */
    private static PreferenceGenerationStrategy createPreferenceStrategy(List<Room> rooms) {
        switch (PREFERENCE_COMPLETION_STRATEGY) {
            case "SmartRandom":
                // Filters by capacity, then randomizes - best for matching course sizes to rooms
                return new SmartRandomPreferenceStrategy(RECOMMENDED_NUM_PREFERENCES, rooms);

            case "Random":
                // Completely random selection from all room types
                return new RandomPreferenceStrategy(RECOMMENDED_NUM_PREFERENCES);

            case "SizeBased":
                // Sorts by how well room capacity matches course size
                return new SizedBasedPreferenceStrategy(RECOMMENDED_NUM_PREFERENCES, rooms);

            default:
                // Default to SmartRandom if unknown strategy specified
                System.out.println("Unknown strategy '" + PREFERENCE_COMPLETION_STRATEGY +
                                 "', defaulting to SmartRandom");
                return new SmartRandomPreferenceStrategy(RECOMMENDED_NUM_PREFERENCES, rooms);
        }
    }

    /**
     * Checks if a course needs preference completion.
     *
     * @param course Course to check
     * @return true if course has fewer than recommended preferences
     */
    public static boolean needsPreferenceCompletion(Course course) {
        List<RoomType> prefs = course.getTypePreferences();
        return prefs == null || prefs.size() < RECOMMENDED_NUM_PREFERENCES;
    }

    /**
     * Counts how many courses need preference completion.
     *
     * @param courses List of courses to check
     * @return Number of courses with incomplete preferences
     */
    public static int countCoursesNeedingCompletion(List<Course> courses) {
        int count = 0;
        for (Course course : courses) {
            if (needsPreferenceCompletion(course)) {
                count++;
            }
        }
        return count;
    }
}
