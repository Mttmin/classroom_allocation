package com.roomallocation;
import java.util.List;

import com.roomallocation.model.Room;
import com.roomallocation.statistics.AllocationStatistics;
import com.roomallocation.statistics.StatisticsCollector;
import com.roomallocation.util.RoomDataLoader;


public class Main {
    public static void main(String[] args) {
        // Create rooms with types
        List<Room> rooms = RoomDataLoader.loadRooms();

               // Configuration parameters
        int numSimulations = 1;     // Number of simulations to run
        int numCourses = 70;         // Number of courses per simulation
        int minSize = 10;            // Minimum course size
        int maxSize = 200;           // Maximum course size
        int changeSize = 40;         // Size threshold for distribution change
        
        System.out.println("Starting Room Allocation Simulations");
        System.out.println("===================================");
        System.out.printf("Running %d simulations with %d courses each%n", 
                         numSimulations, numCourses);
        System.out.println("Course size range: " + minSize + " - " + maxSize);
        System.out.println("Available rooms: " + rooms.size());
        System.out.println();
        
        // Create statistics collector and run simulations
        StatisticsCollector collector = new StatisticsCollector(
            rooms, numSimulations, numCourses, minSize, maxSize, changeSize
        );
        
        List<AllocationStatistics> allStats = collector.runSimulations();
        
        // Individual simulation results are already printed by the collector
        System.out.println("\nSimulation complete.");
    }
}