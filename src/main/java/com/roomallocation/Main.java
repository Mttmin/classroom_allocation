package com.roomallocation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomallocation.allocation.TypeBasedAllocation;
import com.roomallocation.model.Course;
import com.roomallocation.model.Room;
import com.roomallocation.statistics.AllocationStatistics;
import com.roomallocation.statistics.StatisticsCollector;
import com.roomallocation.strategy.*;
import com.roomallocation.util.RoomDataLoader;
// import com.roomallocation.visualization.PythonVisualizer;
import com.simulator.CourseSimulator;

public class Main {

    public static void main(String[] args) {
        try {
            int numSimulations = 100;
            int numCourses = 70;
            int minSize = 10;
            int maxSize = 200;
            int changeSize = 35;
            // Load rooms
            List<Room> rooms = RoomDataLoader.loadRooms();

            // Create statistics collector
            StatisticsCollector collector = new StatisticsCollector(
                rooms, numSimulations, numCourses, minSize, maxSize, changeSize);

            // Optional: Set a seed for reproducible results
            //collector.setSeed(215815L);

            // Add different strategies to test with varying numbers of preferences
            for (int i = 1; i <= 9; i++) {
                collector.addStrategy(new SatisfactionBasedStrategy(i, rooms));
            }

            // Run simulations with all strategies
            List<AllocationStatistics> stats = collector.runSimulations();

            PreferenceGenerationStrategy bestStrategy = new SmartRandomPreferenceStrategy(10, rooms);
            CourseSimulator simulator = new CourseSimulator(bestStrategy);
            List<Course> courses = simulator.generateCourses(numCourses, minSize, maxSize, changeSize);
            TypeBasedAllocation allocator = new TypeBasedAllocation(courses, rooms);
            allocator.allocate();

            // Prepare results directory
            String jsonPath = "room-allocation/src/main/resources/allocation_results.json";

            // Export results to JSON
            Map<String, Object> exportData = new HashMap<>();
            
            // Create simulation parameters map explicitly
            Map<String, Object> simParams = new HashMap<>();
            simParams.put("numSimulations", numSimulations);
            simParams.put("numCourses", numCourses);
            simParams.put("minSize", minSize);
            simParams.put("maxSize", maxSize);
            simParams.put("changeSize", changeSize);
            simParams.put("seed", collector.getSeed());
            
            exportData.put("simulationParameters", simParams);
            exportData.put("statistics", stats.stream()
                .map(AllocationStatistics::toMap)
                .toList());
            exportData.put("allocation", allocator.exportAllocationState());
            // Export to JSON file
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(jsonPath), exportData);
            System.out.println("\nComparison results exported to " + jsonPath);


            // Visualize results
            // PythonVisualizer visualizer = new PythonVisualizer("python");
            // visualizer.visualize(jsonPath);

        } catch (Exception e) {
            System.err.println("Error running comparison: " + e.getMessage());
            e.printStackTrace();
        }
    }
}