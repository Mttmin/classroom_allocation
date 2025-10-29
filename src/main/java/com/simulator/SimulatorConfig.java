package com.simulator;

import java.util.ArrayList;
import java.util.List;

import com.simulator.ClassroomSimulator.DistributionMode;
import com.simulator.ClassroomSimulator.RoomTypeConfig;
import com.simulator.CourseSimulator.CorrelationMode;
import com.simulator.ProfessorSimulator.AvailabilityMode;

/**
 * Centralized configuration for all simulation parameters
 */
public class SimulatorConfig {

    // General settings
    private long randomSeed = System.currentTimeMillis();
    private boolean useSeed = false;

    // Course settings
    private int numCourses = 50;
    private int minCohortSize = 15;
    private int maxCohortSize = 200;
    private int cohortChangeThreshold = 80;
    private double smallClassPercentage = 0.6;
    private double mediumClassPercentage = 0.3;
    private double largeClassPercentage = 0.1;

    // Professor settings
    private int numProfessors = 20;
    private AvailabilityMode professorAvailabilityMode = AvailabilityMode.FULL_TIME;
    private boolean mixedAvailability = false;
    private double fullTimeProfessorPct = 0.5;
    private double mostlyAvailableProfessorPct = 0.3;
    private double partTimeProfessorPct = 0.2;
    private boolean randomProfessorAssignment = false;

    // Classroom settings
    private int numClassrooms = 80;
    private DistributionMode classroomDistributionMode = DistributionMode.REALISTIC;
    private List<RoomTypeConfig> customRoomConfigs = new ArrayList<>();

    // Correlation matrix settings
    private CorrelationMode correlationMode = CorrelationMode.SUBJECT_BASED;
    private int numClusters = 3;
    private double interClusterCorrelation = 0.3;
    private double intraClusterCorrelation = 0.8;

    // Builder pattern for easy configuration
    public static class Builder {
        private SimulatorConfig config = new SimulatorConfig();

        public Builder seed(long seed) {
            config.randomSeed = seed;
            config.useSeed = true;
            return this;
        }

        // Course configuration
        public Builder courses(int num) {
            config.numCourses = num;
            return this;
        }

        public Builder cohortSizeRange(int min, int max, int changeThreshold) {
            config.minCohortSize = min;
            config.maxCohortSize = max;
            config.cohortChangeThreshold = changeThreshold;
            return this;
        }

        public Builder cohortDistribution(double small, double medium, double large) {
            config.smallClassPercentage = small;
            config.mediumClassPercentage = medium;
            config.largeClassPercentage = large;
            return this;
        }

        // Professor configuration
        public Builder professors(int num) {
            config.numProfessors = num;
            return this;
        }

        public Builder professorAvailability(AvailabilityMode mode) {
            config.professorAvailabilityMode = mode;
            config.mixedAvailability = false;
            return this;
        }

        public Builder mixedProfessorAvailability(double fullTime, double mostlyAvailable, double partTime) {
            config.mixedAvailability = true;
            config.fullTimeProfessorPct = fullTime;
            config.mostlyAvailableProfessorPct = mostlyAvailable;
            config.partTimeProfessorPct = partTime;
            return this;
        }

        public Builder randomProfessorAssignment(boolean random) {
            config.randomProfessorAssignment = random;
            return this;
        }

        // Classroom configuration
        public Builder classrooms(int num) {
            config.numClassrooms = num;
            return this;
        }

        public Builder classroomDistribution(DistributionMode mode) {
            config.classroomDistributionMode = mode;
            return this;
        }

        public Builder customRoomConfig(List<RoomTypeConfig> configs) {
            config.customRoomConfigs = new ArrayList<>(configs);
            config.classroomDistributionMode = DistributionMode.CUSTOM;
            return this;
        }

        // Correlation matrix configuration
        public Builder correlationMode(CorrelationMode mode) {
            config.correlationMode = mode;
            return this;
        }

        public Builder clusterConfiguration(int numClusters, double interCluster, double intraCluster) {
            config.correlationMode = CorrelationMode.CLUSTERED;
            config.numClusters = numClusters;
            config.interClusterCorrelation = interCluster;
            config.intraClusterCorrelation = intraCluster;
            return this;
        }

        public SimulatorConfig build() {
            return config;
        }
    }

    // Getters
    public long getRandomSeed() { return randomSeed; }
    public boolean useSeed() { return useSeed; }

    public int getNumCourses() { return numCourses; }
    public int getMinCohortSize() { return minCohortSize; }
    public int getMaxCohortSize() { return maxCohortSize; }
    public int getCohortChangeThreshold() { return cohortChangeThreshold; }
    public double getSmallClassPercentage() { return smallClassPercentage; }
    public double getMediumClassPercentage() { return mediumClassPercentage; }
    public double getLargeClassPercentage() { return largeClassPercentage; }

    public int getNumProfessors() { return numProfessors; }
    public AvailabilityMode getProfessorAvailabilityMode() { return professorAvailabilityMode; }
    public boolean isMixedAvailability() { return mixedAvailability; }
    public double getFullTimeProfessorPct() { return fullTimeProfessorPct; }
    public double getMostlyAvailableProfessorPct() { return mostlyAvailableProfessorPct; }
    public double getPartTimeProfessorPct() { return partTimeProfessorPct; }
    public boolean isRandomProfessorAssignment() { return randomProfessorAssignment; }

    public int getNumClassrooms() { return numClassrooms; }
    public DistributionMode getClassroomDistributionMode() { return classroomDistributionMode; }
    public List<RoomTypeConfig> getCustomRoomConfigs() { return customRoomConfigs; }

    public CorrelationMode getCorrelationMode() { return correlationMode; }
    public int getNumClusters() { return numClusters; }
    public double getInterClusterCorrelation() { return interClusterCorrelation; }
    public double getIntraClusterCorrelation() { return intraClusterCorrelation; }

    // Preset configurations
    public static SimulatorConfig createDefaultConfig() {
        return new Builder().build();
    }

    public static SimulatorConfig createSmallUniversityConfig() {
        return new Builder()
            .courses(30)
            .professors(10)
            .classrooms(40)
            .cohortSizeRange(10, 100, 50)
            .build();
    }

    public static SimulatorConfig createLargeUniversityConfig() {
        return new Builder()
            .courses(200)
            .professors(80)
            .classrooms(150)
            .cohortSizeRange(15, 400, 100)
            .classroomDistribution(DistributionMode.REALISTIC)
            .build();
    }

    public static SimulatorConfig createStressTestConfig() {
        return new Builder()
            .courses(500)
            .professors(50) // Fewer professors than courses to create conflicts
            .classrooms(100) // Limited classrooms
            .mixedProfessorAvailability(0.3, 0.3, 0.4) // Lots of part-time
            .correlationMode(CorrelationMode.CLUSTERED)
            .clusterConfiguration(5, 0.5, 1.2) // High correlations
            .build();
    }

    public static SimulatorConfig createSimpleTestConfig() {
        return new Builder()
            .courses(10)
            .professors(5)
            .classrooms(15)
            .cohortSizeRange(20, 50, 35)
            .professorAvailability(AvailabilityMode.FULL_TIME)
            .correlationMode(CorrelationMode.NONE)
            .seed(12345L) // Fixed seed for reproducibility
            .build();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SimulatorConfig:\n");
        sb.append(String.format("  Seed: %s\n", useSeed ? randomSeed : "Random"));
        sb.append(String.format("  Courses: %d (sizes: %d-%d, threshold: %d)\n",
            numCourses, minCohortSize, maxCohortSize, cohortChangeThreshold));
        sb.append(String.format("  Professors: %d (%s)\n",
            numProfessors, mixedAvailability ? "Mixed availability" : professorAvailabilityMode));
        sb.append(String.format("  Classrooms: %d (%s)\n",
            numClassrooms, classroomDistributionMode));
        sb.append(String.format("  Correlation: %s\n", correlationMode));
        return sb.toString();
    }
}
