package com.simulator;

import com.roomallocation.allocation.TypeBasedAllocation;
import com.roomallocation.constraint.ConstraintValidator;
import com.roomallocation.model.Course;
import com.roomallocation.model.Professor;
import com.roomallocation.model.Room;
import com.roomallocation.scheduler.optimizer.SimulatedAnnealingScheduler;
import com.roomallocation.scheduler.optimizer.Scheduler;
import com.roomallocation.scheduler.scoring.Scoring;
import com.roomallocation.statistics.AllocationStatistics;
import com.roomallocation.statistics.MetricsComparator;
import com.roomallocation.statistics.SchedulerStatistics;
import com.roomallocation.statistics.VisualizationExporter;
import com.roomallocation.strategy.PreferenceGenerationStrategy;
import com.roomallocation.strategy.RandomPreferenceStrategy;
import com.roomallocation.strategy.SmartRandomPreferenceStrategy;
import com.roomallocation.strategy.SizedBasedPreferenceStrategy;
import com.simulator.SimulationRunner.SimulationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simulator for comparing different scheduling algorithms and allocation strategies
 */
public class ComparisonSimulator {

    private SimulatorConfig config;
    private SimulationResult baselineData;

    public ComparisonSimulator(SimulatorConfig config) {
        this.config = config;
    }

    /**
     * Initialize simulation with generated data
     */
    public void initialize() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║       COMPARISON SIMULATOR - Initializing Data            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // Generate baseline data once
        System.out.println("Generating baseline data...");
        SimulationRunner runner = new SimulationRunner(config);

        // Run a quick simulation to generate all the data
        PreferenceGenerationStrategy tempStrategy = new RandomPreferenceStrategy(5);
        runner.setPreferenceStrategy(tempStrategy);
        this.baselineData = runner.runSimulation();

        System.out.println("\nInitialization complete!");
        System.out.println("Courses: " + baselineData.getCourses().size());
        System.out.println("Professors: " + baselineData.getProfessors().size());
        System.out.println("Rooms: " + baselineData.getRooms().size());
        System.out.println();
    }

    /**
     * Run a single scheduler with a preference strategy
     */
    public MetricsComparator.AlgorithmResult runScheduler(String algorithmName,
                                                           PreferenceGenerationStrategy preferenceStrategy) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("Running: " + algorithmName);
        System.out.println("═══════════════════════════════════════════════════════════");

        // Use fresh data for each run
        List<Course> courses = cloneCourses(baselineData.getCourses());
        List<Room> rooms = cloneRooms(baselineData.getRooms());
        Map<String, Professor> professorsMap = convertProfessorsToMap(baselineData.getProfessors());
        double[][] correlationMatrix = baselineData.getCorrelationMatrix();

        // Generate room preferences using strategy
        System.out.println("Generating room preferences with " + preferenceStrategy.getStrategyIdentifier() + "...");
        for (Course course : courses) {
            course.setTypePreferences(preferenceStrategy.generatePreferences(course));
        }

        // Create allocator and scheduler
        TypeBasedAllocation allocator = new TypeBasedAllocation(courses, rooms);
        Scoring scoring = new Scoring();
        ConstraintValidator constraints = new ConstraintValidator(2.0);

        Scheduler scheduler = new SimulatedAnnealingScheduler(
            "SimulatedAnnealingScheduler",
            scoring,
            constraints,
            courses,
            rooms,
            allocator,
            false,
            professorsMap,
            correlationMatrix
        );

        // Run scheduler
        scheduler.runSchedule();

        // Get statistics
        SchedulerStatistics schedulerStats = scheduler.getStatistics();

        // Get allocation statistics
        AllocationStatistics allocationStats = null;
        if (allocator.getSteps() != null && !allocator.getSteps().isEmpty()) {
            allocationStats = new AllocationStatistics(
                preferenceStrategy.getStrategyIdentifier(),
                courses,
                allocator.getSteps()
            );
        }

        System.out.println("\n" + schedulerStats);
        if (allocationStats != null) {
            System.out.println(allocationStats);
        }

        return new MetricsComparator.AlgorithmResult(algorithmName, schedulerStats, allocationStats);
    }

    /**
     * Compare multiple preference strategies
     */
    public void compareStrategies(List<PreferenceGenerationStrategy> strategies, String reportFilename) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║       COMPARISON SIMULATOR - Running Comparisons          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        MetricsComparator comparator = new MetricsComparator();

        for (PreferenceGenerationStrategy strategy : strategies) {
            String algorithmName = "SimulatedAnnealingScheduler + " + strategy.getStrategyIdentifier();

            // Run and collect results
            MetricsComparator.AlgorithmResult result = runScheduler(algorithmName, strategy);

            comparator.addResult(algorithmName, result.getSchedulerStats(), result.getAllocationStats());
        }

        // Print comparison report
        System.out.println("\n\n");
        System.out.println(comparator.generateComparisonReport());

        // Export visualizations
        try {
            System.out.println("\nExporting visualizations...");
            VisualizationExporter.exportComparison(comparator, reportFilename);
            System.out.println("Comparison complete! Check output/visualizations/" + reportFilename + ".html");
        } catch (IOException e) {
            System.err.println("Failed to export visualizations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Run a quick comparison with default configurations
     */
    public void runQuickComparison() {
        List<PreferenceGenerationStrategy> strategies = new ArrayList<>();

        // Different preference strategies
        strategies.add(new RandomPreferenceStrategy(5));
        strategies.add(new SmartRandomPreferenceStrategy(5, baselineData.getRooms()));

        compareStrategies(strategies, "quick_comparison");
    }

    /**
     * Run a comprehensive comparison with all available strategies
     */
    public void runComprehensiveComparison() {
        List<PreferenceGenerationStrategy> strategies = new ArrayList<>();

        // Random strategy with different preference counts
        strategies.add(new RandomPreferenceStrategy(3));
        strategies.add(new RandomPreferenceStrategy(5));
        strategies.add(new RandomPreferenceStrategy(7));

        // Smart random strategy
        strategies.add(new SmartRandomPreferenceStrategy(5, baselineData.getRooms()));
        strategies.add(new SmartRandomPreferenceStrategy(7, baselineData.getRooms()));

        // Size-based strategy
        strategies.add(new SizedBasedPreferenceStrategy(5, baselineData.getRooms()));
        strategies.add(new SizedBasedPreferenceStrategy(7, baselineData.getRooms()));

        compareStrategies(strategies, "comprehensive_comparison");
    }

    /**
     * Compare specific strategies
     */
    public void compareSpecificStrategies(List<PreferenceGenerationStrategy> strategies, String reportName) {
        compareStrategies(strategies, reportName);
    }

    /**
     * Clone courses to avoid modifying original data
     */
    private List<Course> cloneCourses(List<Course> original) {
        List<Course> cloned = new ArrayList<>();
        for (Course c : original) {
            Course copy = new Course(c.getName(), c.getCohortSize());
            copy.setDurationMinutes(c.getDurationMinutes());
            copy.setProfessorId(c.getProfessorId());
            // Preferences will be regenerated
            cloned.add(copy);
        }
        return cloned;
    }

    /**
     * Clone rooms to avoid modifying original data
     */
    private List<Room> cloneRooms(List<Room> original) {
        List<Room> cloned = new ArrayList<>();
        for (Room r : original) {
            cloned.add(new Room(r.getName(), r.getCapacity(), r.getType()));
        }
        return cloned;
    }

    /**
     * Convert professor list to map by ID
     */
    private Map<String, Professor> convertProfessorsToMap(List<Professor> professors) {
        Map<String, Professor> map = new HashMap<>();
        for (Professor p : professors) {
            map.put(p.getId(), p);
        }
        return map;
    }

    // Getters
    public SimulationResult getBaselineData() { return baselineData; }
}
