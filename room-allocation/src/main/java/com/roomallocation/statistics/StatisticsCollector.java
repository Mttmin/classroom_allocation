package com.roomallocation.statistics;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private List<PreferenceGenerationStrategy> strategies;
    private Long seed;
    private List<Long> simulationTimes = new ArrayList<>();

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
        this.strategies = new ArrayList<>();
        this.seed = null;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public void addStrategy(PreferenceGenerationStrategy strategy) {
        strategies.add(strategy);
    }
    public Long getSeed() {
        return seed;
    }
    public List<AllocationStatistics> runSimulations() {
        if (strategies.isEmpty()) {
            throw new IllegalStateException(
                    "No strategies added. Add at least one strategy before running simulations.");
        }

        allStats.clear();
        simulationTimes.clear();
        Random seedGenerator = seed != null ? new Random(seed) : new Random();

        for (int i = 0; i < numSimulations; i++) {
            //System.out.println("\nRunning simulation " + (i + 1) + "/" + numSimulations);
            // long simulationStart = System.nanoTime();

            // Generate a simulation-specific seed
            long simulationSeed = seedGenerator.nextLong();

            for (PreferenceGenerationStrategy strategy : strategies) {
                strategy.setSeed(simulationSeed);
                CourseSimulator simulator = new CourseSimulator(strategy);
                simulator.setSeed(simulationSeed);
                allStats.add(runSimulation(strategy, simulator));
            }

            // long simulationEnd = System.nanoTime();
            // simulationTimes.add((simulationEnd - simulationStart) / 1_000_000); // Convert to milliseconds
        }

        printSummaryStatistics();
        // if you want to print timing statistics, uncomment the line below and system nanotime above
        // printTimingStatistics();
        return allStats;
    }

    private void printTimingStatistics() {
        DoubleSummaryStatistics timeStats = simulationTimes.stream()
                .mapToDouble(Long::doubleValue)
                .summaryStatistics();
        
        System.out.println("\nTiming Statistics:");
        System.out.println("------------------");
        System.out.printf("Average time per simulation: %.2f ms%n", timeStats.getAverage());
        System.out.printf("Min time: %.2f ms%n", timeStats.getMin());
        System.out.printf("Max time: %.2f ms%n", timeStats.getMax());
        System.out.printf("Total time: %.2f ms%n", timeStats.getSum());
    }

    private AllocationStatistics runSimulation(PreferenceGenerationStrategy strategy,
            CourseSimulator simulator) {
        // Generate courses
        List<Course> courses = simulator.generateCourses(numCourses, minSize, maxSize, changeSize);

        // Run allocation and collect steps
        TypeBasedAllocation allocator = new TypeBasedAllocation(courses, rooms);
        allocator.allocate();
        List<AllocationStep> steps = allocator.getSteps();

        // Create and return statistics with strategy identifier
        return new AllocationStatistics(strategy.getStrategyIdentifier(), courses, steps);
    }

    private void printSummaryStatistics() {
        System.out.println("\nSummary Statistics Across " + numSimulations + " Simulations:");
        System.out.println("================================================");

        Map<String, List<AllocationStatistics>> statsByStrategy = allStats.stream()
                .collect(Collectors.groupingBy(stat -> stat.toMap().get("strategyName").toString()));

        statsByStrategy.forEach((strategyId, stats) -> {
            System.out.println("\nStrategy: " + strategyId);
            System.out.println("-------------------");
            printStrategyStatistics(stats);
        });
    }

    private void printStrategyStatistics(List<AllocationStatistics> stats) {
        // Calculate and print statistics
        DoubleSummaryStatistics satisfactionStats = stats.stream()
                .mapToDouble(AllocationStatistics::getSatisfactionRate)
                .summaryStatistics();

        DoubleSummaryStatistics firstChoiceStats = stats.stream()
                .mapToDouble(AllocationStatistics::getFirstChoiceRate)
                .summaryStatistics();

        System.out.printf("Satisfaction Rate: %.1f%% (min: %.1f%%, max: %.1f%%)%n",
                satisfactionStats.getAverage(),
                satisfactionStats.getMin(),
                satisfactionStats.getMax());

        System.out.printf("First Choice Rate: %.1f%% (min: %.1f%%, max: %.1f%%)%n",
                firstChoiceStats.getAverage(),
                firstChoiceStats.getMin(),
                firstChoiceStats.getMax());

        // Add more detailed statistics as needed
    }
}
