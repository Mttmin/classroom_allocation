package com.simulator;

import com.roomallocation.strategy.PreferenceGenerationStrategy;
import com.roomallocation.strategy.RandomPreferenceStrategy;
import com.roomallocation.strategy.SmartRandomPreferenceStrategy;
import com.simulator.ClassroomSimulator.DistributionMode;
import com.simulator.CourseSimulator.CorrelationMode;
import com.simulator.ProfessorSimulator.AvailabilityMode;
import com.simulator.SimulationRunner.SimulationResult;
import com.roomallocation.model.RoomType;
import com.roomallocation.model.Room;

import java.io.IOException;
import java.util.List;

/**
 * Example usage of the simulation framework
 * Demonstrates various configuration options and use cases
 */
public class SimulatorExample {

    public static void main(String[] args) {
        System.out.println("===================================");
        System.out.println("Classroom Allocation Simulator");
        System.out.println("===================================\n");

        // Run different simulation scenarios
        runUniversitySimulation();
        runSmartRandomSimulation();

        // NEW: Run comparison simulator to compare different algorithms
        runComparisonDemo();
    }

    /**
     * Example 1: Default configuration
     */
    private static void runDefaultSimulation() {
        System.out.println("\n--- EXAMPLE 1: Default Configuration ---\n");

        SimulatorConfig config = SimulatorConfig.createDefaultConfig();
        PreferenceGenerationStrategy strategy = new RandomPreferenceStrategy(5);

        SimulationRunner runner = new SimulationRunner(config, strategy);
        SimulationResult result = runner.runSimulation();
        result.printSummary();

        // Export to JSON
        try {
            JsonExporter.exportAll(result, "./simulation_output/default");
        } catch (IOException e) {
            System.err.println("Error exporting JSON: " + e.getMessage());
        }
    }

    /**
     * Example 2: University simulation
     */
    private static void runUniversitySimulation() {
        System.out.println("\n--- EXAMPLE 2: University ---\n");

        SimulatorConfig config = SimulatorConfig.createLargeUniversityConfig();
        PreferenceGenerationStrategy strategy = new RandomPreferenceStrategy(5);

        SimulationRunner runner = new SimulationRunner(config, strategy);
        SimulationResult result = runner.runSimulation();
        result.printSummary();

        // Export to JSON
        try {
            JsonExporter.exportAll(result, "src\\main\\resources");
        } catch (IOException e) {
            System.err.println("Error exporting JSON: " + e.getMessage());
        }
    }

    /**
     * Example 3: Stress test with difficult constraints
     */
    private static void runStressTestSimulation() {
        System.out.println("\n--- EXAMPLE 3: Stress Test ---\n");

        SimulatorConfig config = SimulatorConfig.createStressTestConfig();
        PreferenceGenerationStrategy strategy = new RandomPreferenceStrategy(5);

        SimulationRunner runner = new SimulationRunner(config, strategy);
        SimulationResult result = runner.runSimulation();
        result.printSummary();

        // Export to JSON
        try {
            JsonExporter.exportAll(result, "./simulation_output/stress_test");
        } catch (IOException e) {
            System.err.println("Error exporting JSON: " + e.getMessage());
        }
    }

    private static void runCustomConfigurationSimulation() {
        System.out.println("\n--- EXAMPLE 4: Custom Configuration ---\n");

        // Build a completely custom configuration
        SimulatorConfig config = new SimulatorConfig.Builder()
            .seed(42L) // Fixed seed for reproducibility
            .courses(75)
            .cohortSizeRange(10, 150, 60)
            .cohortDistribution(0.5, 0.35, 0.15)
            .professors(25)
            .mixedProfessorAvailability(0.4, 0.4, 0.2)
            .randomProfessorAssignment(true)
            .classrooms(100)
            .classroomDistribution(DistributionMode.REALISTIC)
            .correlationMode(CorrelationMode.CLUSTERED)
            .clusterConfiguration(4, 0.25, 0.9)
            .build();

        PreferenceGenerationStrategy strategy = new RandomPreferenceStrategy(5);

        SimulationRunner runner = new SimulationRunner(config, strategy);
        SimulationResult result = runner.runSimulation();
        result.printSummary();

        // Export to JSON
        try {
            JsonExporter.exportAll(result, "./simulation_output/custom");
        } catch (IOException e) {
            System.err.println("Error exporting JSON: " + e.getMessage());
        }
    }

    public static void demonstrateProfessorAvailability() {
        System.out.println("\n--- DEMO: Professor Availability Modes ---\n");

        AvailabilityMode[] modes = {
            AvailabilityMode.FULL_TIME,
            AvailabilityMode.MOSTLY_AVAILABLE_95,
            AvailabilityMode.PART_TIME_50,
            AvailabilityMode.MORNING_ONLY,
            AvailabilityMode.THREE_DAYS_WEEK
        };

        for (AvailabilityMode mode : modes) {
            System.out.println("Testing mode: " + mode);

            SimulatorConfig config = new SimulatorConfig.Builder()
                .courses(20)
                .professors(10)
                .professorAvailability(mode)
                .classrooms(30)
                .correlationMode(CorrelationMode.NONE)
                .build();

            PreferenceGenerationStrategy strategy = new RandomPreferenceStrategy(5);
            SimulationRunner runner = new SimulationRunner(config, strategy);
            SimulationResult result = runner.runSimulation();

            System.out.println("  Created " + result.getProfessors().size() + " professors with " + mode + " availability\n");
        }
    }


    public static void demonstrateCorrelationModes() {
        System.out.println("\n--- DEMO: Correlation Matrix Modes ---\n");

        CorrelationMode[] modes = {
            CorrelationMode.NONE,
            CorrelationMode.FULLY_RANDOM,
            CorrelationMode.SUBJECT_BASED,
            CorrelationMode.CLUSTERED
        };

        for (CorrelationMode mode : modes) {
            System.out.println("Testing mode: " + mode);

            SimulatorConfig config = new SimulatorConfig.Builder()
                .courses(30)
                .professors(15)
                .classrooms(40)
                .correlationMode(mode)
                .clusterConfiguration(3, 0.3, 0.8) // Only used for CLUSTERED mode
                .build();

            PreferenceGenerationStrategy strategy = new RandomPreferenceStrategy(5);
            SimulationRunner runner = new SimulationRunner(config, strategy);
            SimulationResult result = runner.runSimulation();

            System.out.println("  Generated " + result.getCorrelationMatrix().length + "x" +
                result.getCorrelationMatrix().length + " correlation matrix with mode: " + mode + "\n");
        }
    }

    public static void demonstrateClassroomDistribution() {
        System.out.println("\n--- DEMO: Classroom Distribution Modes ---\n");

        DistributionMode[] modes = {
            DistributionMode.UNIFORM,
            DistributionMode.REALISTIC,
            DistributionMode.SMALL_FOCUSED,
            DistributionMode.LARGE_FOCUSED
        };

        for (DistributionMode mode : modes) {
            System.out.println("Testing mode: " + mode);

            SimulatorConfig config = new SimulatorConfig.Builder()
                .courses(40)
                .professors(15)
                .classrooms(60)
                .classroomDistribution(mode)
                .correlationMode(CorrelationMode.SUBJECT_BASED)
                .build();

            PreferenceGenerationStrategy strategy = new RandomPreferenceStrategy(5);
            SimulationRunner runner = new SimulationRunner(config, strategy);
            SimulationResult result = runner.runSimulation();

            System.out.println("  Generated " + result.getRooms().size() + " rooms with distribution: " + mode);

            // Print room type breakdown
            int[] typeCounts = new int[RoomType.values().length];
            for (var room : result.getRooms()) {
                typeCounts[room.getType().ordinal()]++;
            }

            System.out.println("  Room type breakdown:");
            for (int i = 0; i < RoomType.values().length; i++) {
                if (typeCounts[i] > 0) {
                    System.out.println("    " + RoomType.values()[i] + ": " + typeCounts[i]);
                }
            }
            System.out.println();
        }
    }

    private static void runSmartRandomSimulation() {
        System.out.println("\n--- EXAMPLE 5: Smart Random Strategy ---\n");

        // PHASE 1: Generate rooms first
        SimulatorConfig config = SimulatorConfig.createDefaultConfig();
        SimulationRunner runner = new SimulationRunner(config);
        List<Room> rooms = runner.generateRoomsOnly();
        System.out.println("Phase 1: Pre-generated " + rooms.size() + " rooms for analysis");

        // PHASE 2: Create smart strategy and run simulation
        PreferenceGenerationStrategy smartStrategy = new SmartRandomPreferenceStrategy(5, rooms);
        runner.setPreferenceStrategy(smartStrategy);
        System.out.println("Phase 2: Running simulation with smart random preferences\n");

        SimulationResult result = runner.runSimulation();
        result.printSummary();

        // Export to JSON
        try {
            JsonExporter.exportAll(result, "src\\main\\resources");
        } catch (IOException e) {
            System.err.println("Error exporting JSON: " + e.getMessage());
        }

        System.out.println("\nSmart Random Strategy Benefits:");
        System.out.println("  - Preferences are based on actual room capacities");
        System.out.println("  - Courses only prefer room types that can accommodate their size");
        System.out.println("  - Reduces infeasible allocations and improves scheduling efficiency");
    }

    /**
     * NEW: Demonstrate the comparison simulator with statistics and visualizations
     */
    private static void runComparisonDemo() {
        System.out.println("\n\n");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("  COMPARISON DEMO: Algorithm Performance Analysis");
        System.out.println("═══════════════════════════════════════════════════════════\n");

        // Create configuration for comparison
        SimulatorConfig config = SimulatorConfig.createDefaultConfig();

        // Initialize the comparison simulator
        ComparisonSimulator comparisonSim = new ComparisonSimulator(config);
        comparisonSim.initialize();

        System.out.println("\n--- Running Quick Comparison (Random vs SmartRandom) ---\n");
        comparisonSim.runQuickComparison();

        System.out.println("\n\n--- Running Comprehensive Comparison (All Strategies) ---\n");
        comparisonSim.runComprehensiveComparison();

        System.out.println("\n");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("  Comparisons complete!");
        System.out.println("  Check the following files for interactive visualizations:");
        System.out.println("  - output/visualizations/quick_comparison.html");
        System.out.println("  - output/visualizations/comprehensive_comparison.html");
        System.out.println("═══════════════════════════════════════════════════════════");
    }
}
