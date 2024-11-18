package com.roomallocation.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.roomallocation.allocation.AllocationStep;
import com.roomallocation.allocation.TypeBasedAllocation;
import com.roomallocation.model.Course;
import com.roomallocation.model.Room;
import com.roomallocation.simulator.CourseSimulator;
import com.roomallocation.strategy.PreferenceGenerationStrategy;
import com.roomallocation.strategy.SizedBasedPreferenceStrategy;
import com.roomallocation.strategy.SmartRandomPreferenceStrategy;

public class StatisticsCollector {
    private final List<Room> rooms;
    private final int numSimulations;
    private final int numCourses;
    private final int minSize;
    private final int maxSize;
    private final int changeSize;
    private int numPreferences = 10;
    private List<AllocationStatistics> allStats;

    public void setNumPreferences(int numPreferences) {
        this.numPreferences = numPreferences;
    }
    public StatisticsCollector(List<Room> rooms, int numSimulations, 
                             int numCourses, int minSize, int maxSize, int changeSize) {
        this.rooms = rooms;
        this.numSimulations = numSimulations;
        this.numCourses = numCourses;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.changeSize = changeSize;
        this.allStats = new ArrayList<>();
    }

    public List<AllocationStatistics> runSimulations() {
        // Create strategies
        //PreferenceGenerationStrategy randomStrategy = new RandomPreferenceStrategy(numPreferences);
        PreferenceGenerationStrategy sizeStrategy = new SizedBasedPreferenceStrategy(numPreferences, rooms);
        PreferenceGenerationStrategy smartRandomStrategy = new SmartRandomPreferenceStrategy(numPreferences, rooms);
        
        // Clear previous results
        allStats.clear();
        
        for (int i = 0; i < numSimulations; i++) {
            System.out.println("Running simulation " + (i + 1) + "/" + numSimulations);
            
            // Run simulation with each strategy
            //allStats.add(runSimulation("Random", randomStrategy));
            allStats.add(runSimulation("SizeBased", sizeStrategy));
            allStats.add(runSimulation("SmartRandom", smartRandomStrategy));
        }
        
        // Print summary statistics
        printSummaryStatistics();
        
        return allStats;
    }

    private AllocationStatistics runSimulation(String strategyName, 
                                             PreferenceGenerationStrategy strategy) {
        // Generate courses
        CourseSimulator simulator = new CourseSimulator(strategy);
        List<Course> courses = simulator.generateCourses(numCourses, minSize, maxSize, changeSize);
        
        // Run allocation and collect steps
        TypeBasedAllocation allocator = new TypeBasedAllocation(courses, rooms);
        allocator.allocate();
        List<AllocationStep> steps = allocator.getSteps();
        
        // Create and return statistics
        return new AllocationStatistics(strategyName, courses, steps);
    }

    private void printSummaryStatistics() {
        System.out.println("\nSummary Statistics Across " + numSimulations + " Simulations:");
        System.out.println("================================================");

        // Group statistics by strategy
        Map<String, List<AllocationStatistics>> statsByStrategy = allStats.stream()
            .collect(Collectors.groupingBy(stat -> stat.toMap().get("strategyName").toString()));

        // Print average metrics for each strategy
        statsByStrategy.forEach((strategy, stats) -> {
            System.out.println("\nStrategy: " + strategy);
            System.out.println("-------------------");
            double avgSatisfaction = stats.stream().mapToDouble(AllocationStatistics::getSatisfactionRate).average().orElse(0);
            double avgFirstChoice = stats.stream().mapToDouble(AllocationStatistics::getFirstChoiceRate).average().orElse(0);
            double avgHighRank = stats.stream().mapToDouble(AllocationStatistics::getHighRankRate).average().orElse(0);
            double avgUnallocated = stats.stream().mapToDouble(AllocationStatistics::getUnallocatedRate).average().orElse(0);
            double avgChoice = stats.stream().mapToDouble(AllocationStatistics::getAverageChoice).average().orElse(0);
            double avgSteps = stats.stream().mapToDouble(AllocationStatistics::getNumAllocationSteps).average().orElse(0);

            System.out.printf("Average Satisfaction Rate: %.1f%%%n", avgSatisfaction);
            System.out.printf("Average First Choice Rate: %.1f%%%n", avgFirstChoice);
            System.out.printf("Average High Rank Rate (4+): %.1f%%%n", avgHighRank);
            System.out.printf("Average Unallocated Rate: %.1f%%%n", avgUnallocated);
            System.out.printf("Average Choice Number: %.2f%n", avgChoice);
            System.out.printf("Average Number of Steps: %.1f%n", avgSteps);
        });
    }
}
