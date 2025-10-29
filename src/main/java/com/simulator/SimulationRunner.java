package com.simulator;

import com.roomallocation.model.Course;
import com.roomallocation.model.Professor;
import com.roomallocation.model.Room;
import com.roomallocation.strategy.PreferenceGenerationStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main orchestrator class that runs all simulators and generates complete test data
 */
public class SimulationRunner {
    private final SimulatorConfig config;
    private PreferenceGenerationStrategy preferenceStrategy;

    private CourseSimulator courseSimulator;
    private ProfessorSimulator professorSimulator;
    private ClassroomSimulator classroomSimulator;

    private List<Course> courses;
    private List<Professor> professors;
    private List<Room> rooms;
    private double[][] correlationMatrix;

    private boolean needsRoomBasedStrategy = false;

    public SimulationRunner(SimulatorConfig config, PreferenceGenerationStrategy preferenceStrategy) {
        this.config = config;
        this.preferenceStrategy = preferenceStrategy;
        initializeSimulators();
    }

    /**
     * Constructor for strategies that need room data (like SmartRandomPreferenceStrategy)
     * The strategy will be set after rooms are generated
     */
    public SimulationRunner(SimulatorConfig config) {
        this.config = config;
        this.preferenceStrategy = null;
        this.needsRoomBasedStrategy = true;
        initializeSimulators();
    }

    private void initializeSimulators() {
        courseSimulator = new CourseSimulator(preferenceStrategy);
        professorSimulator = new ProfessorSimulator();
        classroomSimulator = new ClassroomSimulator();

        // Set seed if configured
        if (config.useSeed()) {
            courseSimulator.setSeed(config.getRandomSeed());
            professorSimulator.setSeed(config.getRandomSeed());
            classroomSimulator.setSeed(config.getRandomSeed());
        }
    }

    /**
     * Run all simulations and generate complete dataset
     */
    public SimulationResult runSimulation() {
        System.out.println("Starting simulation with configuration:");
        System.out.println(config);
        System.out.println();

        // Step 1: Generate professors
        System.out.println("Generating professors...");
        generateProfessors();
        System.out.println("  Created " + professors.size() + " professors");

        // Step 2: Generate courses (without preferences if using room-based strategy)
        System.out.println("Generating courses...");
        generateCourses();
        System.out.println("  Created " + courses.size() + " courses");

        // Step 3: Assign professors to courses
        System.out.println("Assigning professors to courses...");
        assignProfessorsToCourses();
        System.out.println("  Assignments complete");

        // Step 4: Set course durations
        System.out.println("Setting course durations...");
        courseSimulator.setRandomDurations(courses);
        System.out.println("  Durations assigned");

        // Step 5: Generate correlation matrix
        System.out.println("Generating correlation matrix...");
        generateCorrelationMatrix();
        System.out.println("  Correlation matrix created (" + correlationMatrix.length + "x" + correlationMatrix.length + ")");

        // Step 6: Generate classrooms
        System.out.println("Generating classrooms...");
        generateClassrooms();
        System.out.println("  Created " + rooms.size() + " classrooms");

        // Step 7: Generate preferences if using room-based strategy
        if (needsRoomBasedStrategy && preferenceStrategy != null) {
            System.out.println("Generating room preferences based on classroom data...");
            courseSimulator.generatePreferencesForCourses(courses);
            System.out.println("  Preferences generated");
        }

        System.out.println("\nSimulation complete!");

        return new SimulationResult(courses, professors, rooms, correlationMatrix, config);
    }

    /**
     * Set the preference generation strategy (used for room-based strategies after rooms are generated)
     */
    public void setPreferenceStrategy(PreferenceGenerationStrategy strategy) {
        this.preferenceStrategy = strategy;
        // Update the course simulator with the new strategy
        this.courseSimulator = new CourseSimulator(strategy);
        if (config.useSeed()) {
            courseSimulator.setSeed(config.getRandomSeed());
        }
    }

    private void generateProfessors() {
        if (config.isMixedAvailability()) {
            professors = professorSimulator.generateProfessorsWithMixedAvailability(
                config.getNumProfessors(),
                config.getFullTimeProfessorPct(),
                config.getMostlyAvailableProfessorPct(),
                config.getPartTimeProfessorPct()
            );
        } else {
            professors = professorSimulator.generateProfessors(
                config.getNumProfessors(),
                config.getProfessorAvailabilityMode()
            );
        }
    }

    private void generateCourses() {
        courses = courseSimulator.generateCourses(
            config.getNumCourses(),
            config.getMinCohortSize(),
            config.getMaxCohortSize(),
            config.getCohortChangeThreshold(),
            config.getSmallClassPercentage(),
            config.getMediumClassPercentage(),
            config.getLargeClassPercentage()
        );
    }

    private void assignProfessorsToCourses() {
        List<String> professorIds = professors.stream()
            .map(Professor::getId)
            .collect(Collectors.toList());

        courseSimulator.assignProfessorsToCourses(
            courses,
            professorIds,
            config.isRandomProfessorAssignment()
        );
    }

    private void generateCorrelationMatrix() {
        correlationMatrix = courseSimulator.generateCorrelationMatrix(
            courses,
            config.getCorrelationMode(),
            config.getNumClusters(),
            config.getInterClusterCorrelation(),
            config.getIntraClusterCorrelation()
        );
    }

    private void generateClassrooms() {
        if (config.getClassroomDistributionMode() == ClassroomSimulator.DistributionMode.CUSTOM
            && !config.getCustomRoomConfigs().isEmpty()) {
            rooms = classroomSimulator.generateClassroomsFromConfig(config.getCustomRoomConfigs());
        } else {
            rooms = classroomSimulator.generateClassrooms(
                config.getNumClassrooms(),
                config.getClassroomDistributionMode()
            );
        }
    }

    // Getters for accessing generated data
    public List<Course> getCourses() { return courses; }
    public List<Professor> getProfessors() { return professors; }
    public List<Room> getRooms() { return rooms; }
    public double[][] getCorrelationMatrix() { return correlationMatrix; }

    /**
     * Helper method to generate rooms before running full simulation
     * Useful for strategies that need room data
     */
    public List<Room> generateRoomsOnly() {
        generateClassrooms();
        return rooms;
    }

    /**
     * Result container with all simulation outputs
     */
    public static class SimulationResult {
        private final List<Course> courses;
        private final List<Professor> professors;
        private final List<Room> rooms;
        private final double[][] correlationMatrix;
        private final SimulatorConfig config;

        public SimulationResult(List<Course> courses, List<Professor> professors,
                              List<Room> rooms, double[][] correlationMatrix,
                              SimulatorConfig config) {
            this.courses = new ArrayList<>(courses);
            this.professors = new ArrayList<>(professors);
            this.rooms = new ArrayList<>(rooms);
            this.correlationMatrix = correlationMatrix;
            this.config = config;
        }

        public List<Course> getCourses() { return courses; }
        public List<Professor> getProfessors() { return professors; }
        public List<Room> getRooms() { return rooms; }
        public double[][] getCorrelationMatrix() { return correlationMatrix; }
        public SimulatorConfig getConfig() { return config; }

        /**
         * Print summary statistics
         */
        public void printSummary() {
            System.out.println("\n===== SIMULATION SUMMARY =====");
            System.out.println("\nConfiguration:");
            System.out.println(config);

            System.out.println("\nCourses:");
            System.out.println("  Total: " + courses.size());
            int minSize = courses.stream().mapToInt(Course::getCohortSize).min().orElse(0);
            int maxSize = courses.stream().mapToInt(Course::getCohortSize).max().orElse(0);
            double avgSize = courses.stream().mapToInt(Course::getCohortSize).average().orElse(0);
            System.out.println("  Cohort sizes: " + minSize + " - " + maxSize + " (avg: " + String.format("%.1f", avgSize) + ")");

            System.out.println("\nProfessors:");
            System.out.println("  Total: " + professors.size());
            System.out.println("  Courses per professor: " + String.format("%.1f", (double)courses.size() / professors.size()));

            System.out.println("\nRooms:");
            System.out.println("  Total: " + rooms.size());
            int minCap = rooms.stream().mapToInt(Room::getCapacity).min().orElse(0);
            int maxCap = rooms.stream().mapToInt(Room::getCapacity).max().orElse(0);
            double avgCap = rooms.stream().mapToInt(Room::getCapacity).average().orElse(0);
            System.out.println("  Capacities: " + minCap + " - " + maxCap + " (avg: " + String.format("%.1f", avgCap) + ")");

            System.out.println("\nCorrelation Matrix:");
            System.out.println("  Size: " + correlationMatrix.length + "x" + correlationMatrix.length);
            printCorrelationStats();

            System.out.println("\n==============================\n");
        }

        private void printCorrelationStats() {
            int hardConstraints = 0;
            int highCorrelations = 0;
            int mediumCorrelations = 0;
            int lowCorrelations = 0;

            int n = correlationMatrix.length;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) { // Only upper triangle
                    double corr = correlationMatrix[i][j];
                    if (corr >= 2.0) {
                        hardConstraints++;
                    } else if (corr >= 1.0) {
                        highCorrelations++;
                    } else if (corr >= 0.5) {
                        mediumCorrelations++;
                    } else if (corr > 0.0) {
                        lowCorrelations++;
                    }
                }
            }

            int totalPairs = (n * (n - 1)) / 2;
            System.out.println("  Hard constraints (>= 2.0): " + hardConstraints +
                " (" + String.format("%.1f%%", 100.0 * hardConstraints / totalPairs) + ")");
            System.out.println("  High correlation (>= 1.0): " + highCorrelations +
                " (" + String.format("%.1f%%", 100.0 * highCorrelations / totalPairs) + ")");
            System.out.println("  Medium correlation (>= 0.5): " + mediumCorrelations +
                " (" + String.format("%.1f%%", 100.0 * mediumCorrelations / totalPairs) + ")");
            System.out.println("  Low correlation (> 0.0): " + lowCorrelations +
                " (" + String.format("%.1f%%", 100.0 * lowCorrelations / totalPairs) + ")");
        }
    }
}
