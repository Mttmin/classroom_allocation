package com.roomallocation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomallocation.allocation.TypeBasedAllocation;
import com.roomallocation.model.Course;
import com.roomallocation.model.Room;
import com.roomallocation.simulator.CourseSimulator;
import com.roomallocation.statistics.AllocationStatistics;
import com.roomallocation.statistics.StatisticsCollector;
import com.roomallocation.strategy.PreferenceGenerationStrategy;
import com.roomallocation.strategy.SmartRandomPreferenceStrategy;
import com.roomallocation.util.RoomDataLoader;

public class Main {
    public static void main(String[] args) {
        try {
            // Load rooms
            List<Room> rooms = RoomDataLoader.loadRooms();

            // Create strategy and simulator
            PreferenceGenerationStrategy strategy = new SmartRandomPreferenceStrategy(10, rooms);
            CourseSimulator simulator = new CourseSimulator(strategy);

            // Generate courses
            List<Course> courses = simulator.generateCourses(70, 10, 200, 40);

            // Run allocation
            TypeBasedAllocation allocator = new TypeBasedAllocation(courses, rooms);
            allocator.allocate();

            // Run statistics collector
            StatisticsCollector collector = new StatisticsCollector(rooms, 1, 70, 10, 200, 40);
            List<AllocationStatistics> stats = collector.runSimulations();

            // Combine allocation state and statistics into one export
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("allocation", allocator.exportAllocationState());
            exportData.put("statistics", stats.stream()
                    .map(AllocationStatistics::toMap)
                    .toList());

            // Export combined data to JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File("src/main/resources/allocation_results.json"), exportData);
            System.out.println("Allocation results and statistics exported to allocation_results.json");

        } catch (Exception e) {
            System.err.println("Error running allocation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}