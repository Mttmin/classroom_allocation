package com.roomallocation;
import java.util.List;
import java.util.Map;

import com.roomallocation.model.Course;
import com.roomallocation.model.Room;
import com.roomallocation.strategy.PreferenceGenerationStrategy;
import com.roomallocation.strategy.SizedBasedPreferenceStrategy;
import com.roomallocation.strategy.SmartRandomPreferenceStrategy;
import com.roomallocation.simulator.CourseSimulator;
import com.roomallocation.allocation.TypeBasedAllocation;
import com.roomallocation.util.RoomDataLoader;


public class Main {
    public static void main(String[] args) {
        // Create rooms with types
        List<Room> rooms = RoomDataLoader.loadRooms();
        PreferenceGenerationStrategy sizestrategy = new SizedBasedPreferenceStrategy(10, rooms);
        PreferenceGenerationStrategy smartRandom = new SmartRandomPreferenceStrategy(10, rooms);
        // Create courses with different sizes
        // Create simulator with a strategy (you can change this to use different strategies)
        CourseSimulator simulator = new CourseSimulator(smartRandom);
        List<Course> courses = simulator.generateCourses(70, 10, 200, 40);

        // for (Course course : courses) {
        //     System.out.println(course.getName() + " preferences: " + course.getTypePreferences());
        // }
        // Run allocation
        TypeBasedAllocation allocator = new TypeBasedAllocation(courses, rooms);
        Map<String, String> finalAssignments = allocator.allocate();

        System.out.println("\nFinal Assignments:");
        courses.stream()
            .filter(course -> course.getAssignedRoom() != null)
            .forEach(course -> System.out.printf("%s -> %s (Choice #%d)%n", 
                course.getName(), 
                course.getAssignedRoom(), 
                course.getChoiceNumber()));
    }
}