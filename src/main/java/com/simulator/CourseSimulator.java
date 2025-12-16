package com.simulator;

import com.roomallocation.model.Course;
import com.roomallocation.strategy.PreferenceGenerationStrategy;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CourseSimulator {
    private final PreferenceGenerationStrategy strategy;
    private final Random random;

    /**
     * Correlation matrix generation mode
     */
    public enum CorrelationMode {
        FULLY_RANDOM,      // Completely random correlations
        CLUSTERED,         // Simulates program clusters with high internal correlation
        SUBJECT_BASED,     // Based on subject prefix (current implementation)
        NONE              // All correlations are 0.0 except diagonal
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    public CourseSimulator(PreferenceGenerationStrategy strategy) {
        this.strategy = strategy;
        this.random = new Random();
    }

    private String generateSubject() {
        String[] subjects = {"MAT", "MAP", "HSS", "LAN", "MEC", "ECO", "BIO", "INF", "PHY", "CHI"};
        String matter = subjects[random.nextInt(subjects.length)];
        String number = Integer.toString(300 + random.nextInt(200));
        return matter + number;
    }

    public List<Course> generateCourses(int numCourses, int minSize, int maxSize, int changeSize) {
        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) {
            int size;
            if (random.nextDouble() < 0.9) {
                size = random.nextInt(changeSize - minSize + 1) + minSize;
            } else {
                size = random.nextInt(maxSize - changeSize + 1) + changeSize;
            }
            courses.add(new Course(generateSubject(), size));
            // Only generate preferences if strategy is available
            if (strategy != null) {
                courses.get(i).setTypePreferences(strategy.generatePreferences(courses.get(i)));
            }
        }
        return courses;
    }

    /**
     * Generate courses with custom cohort size distribution
     */
    public List<Course> generateCourses(int numCourses, int minSize, int maxSize, int changeSize,
                                       double smallClassPct, double mediumClassPct, double largeClassPct) {
        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) {
            int size;
            double rand = random.nextDouble();

            if (rand < smallClassPct) {
                // Small class: minSize to changeSize
                size = random.nextInt(changeSize - minSize + 1) + minSize;
            } else if (rand < smallClassPct + mediumClassPct) {
                // Medium class: changeSize to middle range
                int midPoint = (changeSize + maxSize) / 2;
                size = random.nextInt(midPoint - changeSize + 1) + changeSize;
            } else {
                // Large class: upper range
                int midPoint = (changeSize + maxSize) / 2;
                size = random.nextInt(maxSize - midPoint + 1) + midPoint;
            }

            courses.add(new Course(generateSubject(), size));
            // Only generate preferences if strategy is available
            if (strategy != null) {
                courses.get(i).setTypePreferences(strategy.generatePreferences(courses.get(i)));
            }
        }
        return courses;
    }

    /**
     * Generate preferences for existing courses using the configured strategy
     */
    public void generatePreferencesForCourses(List<Course> courses) {
        if (strategy != null) {
            for (Course course : courses) {
                course.setTypePreferences(strategy.generatePreferences(course));
            }
        }
    }

    /**
     * Assign professors to courses (round-robin or random)
     */
    public void assignProfessorsToCourses(List<Course> courses, List<String> professorIds, boolean randomAssignment) {
        if (professorIds.isEmpty()) {
            return;
        }

        if (randomAssignment) {
            // Random assignment
            for (Course course : courses) {
                String profId = professorIds.get(random.nextInt(professorIds.size()));
                course.setProfessorIds(List.of(profId));
            }
        } else {
            // Round-robin assignment
            for (int i = 0; i < courses.size(); i++) {
                String profId = professorIds.get(i % professorIds.size());
                courses.get(i).setProfessorIds(List.of(profId));
            }
        }
    }

    /**
     * Set random durations for courses
     */
    public void setRandomDurations(List<Course> courses) {
        int[] durations = {60, 90, 120, 180, 200};
        for (Course course : courses) {
            int duration = durations[random.nextInt(durations.length)];
            course.setDurationMinutes(duration);
        }
    }

    /**
     * Generate correlation matrix for courses
     * Correlation values:
     * - 2.0: Same program (hard constraint - cannot overlap)
     * - 1.0: High correlation (many shared students)
     * - 0.5: Medium correlation (some shared students)
     * - 0.0: No correlation (no shared students)
     */
    public double[][] generateCorrelationMatrix(List<Course> courses) {
        return generateCorrelationMatrix(courses, CorrelationMode.SUBJECT_BASED);
    }

    /**
     * Generate correlation matrix with specified mode
     */
    public double[][] generateCorrelationMatrix(List<Course> courses, CorrelationMode mode) {
        return generateCorrelationMatrix(courses, mode, 3, 0.3, 0.8);
    }

    /**
     * Generate correlation matrix with full configuration
     *
     * @param courses List of courses
     * @param mode Correlation generation mode
     * @param numClusters Number of program clusters (for CLUSTERED mode)
     * @param interClusterCorrelation Average correlation between different clusters
     * @param intraClusterCorrelation Average correlation within same cluster
     */
    public double[][] generateCorrelationMatrix(List<Course> courses, CorrelationMode mode,
                                               int numClusters, double interClusterCorrelation,
                                               double intraClusterCorrelation) {
        int n = courses.size();
        double[][] correlationMatrix = new double[n][n];

        switch (mode) {
            case FULLY_RANDOM:
                correlationMatrix = generateFullyRandomMatrix(courses);
                break;

            case CLUSTERED:
                correlationMatrix = generateClusteredMatrix(courses, numClusters,
                    interClusterCorrelation, intraClusterCorrelation);
                break;

            case SUBJECT_BASED:
                correlationMatrix = generateSubjectBasedMatrix(courses);
                break;

            case NONE:
                // All zeros except diagonal (which is also 0 - no self-conflict)
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        correlationMatrix[i][j] = 0.0;
                    }
                }
                break;
        }

        return correlationMatrix;
    }

    /**
     * Generate fully random correlation matrix
     */
    private double[][] generateFullyRandomMatrix(List<Course> courses) {
        int n = courses.size();
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0.0;
                } else if (j < i) {
                    // Use symmetric value
                    matrix[i][j] = matrix[j][i];
                } else {
                    // Generate random correlation
                    double rand = random.nextDouble();
                    if (rand < 0.05) {
                        matrix[i][j] = 2.0; // 5% hard constraints
                    } else if (rand < 0.15) {
                        matrix[i][j] = 1.5; // 10% very high correlation
                    } else if (rand < 0.25) {
                        matrix[i][j] = 1.0; // 10% high correlation
                    } else if (rand < 0.40) {
                        matrix[i][j] = 0.5; // 15% medium correlation
                    } else {
                        matrix[i][j] = 0.0; // 60% no correlation
                    }
                }
            }
        }

        return matrix;
    }

    /**
     * Generate clustered correlation matrix
     * Simulates multiple programs with high internal correlation
     */
    private double[][] generateClusteredMatrix(List<Course> courses, int numClusters,
                                              double interClusterCorr, double intraClusterCorr) {
        int n = courses.size();
        double[][] matrix = new double[n][n];

        // Assign each course to a cluster
        Map<Integer, Integer> courseToCluster = new HashMap<>();
        for (int i = 0; i < n; i++) {
            courseToCluster.put(i, i % numClusters);
        }

        // Generate correlations based on cluster membership
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0.0;
                } else if (j < i) {
                    // Use symmetric value
                    matrix[i][j] = matrix[j][i];
                } else {
                    int cluster1 = courseToCluster.get(i);
                    int cluster2 = courseToCluster.get(j);

                    if (cluster1 == cluster2) {
                        // Same cluster - high correlation
                        double variance = 0.3 * random.nextGaussian(); // Add some variance
                        double correlation = Math.max(0.0, Math.min(2.0, intraClusterCorr + variance));

                        // Some courses in same cluster should be hard constraints
                        if (random.nextDouble() < 0.15) {
                            matrix[i][j] = 2.0;
                        } else {
                            matrix[i][j] = correlation;
                        }
                    } else {
                        // Different clusters - lower correlation
                        double variance = 0.2 * random.nextGaussian();
                        double correlation = Math.max(0.0, Math.min(1.5, interClusterCorr + variance));
                        matrix[i][j] = correlation;
                    }
                }
            }
        }

        return matrix;
    }

    /**
     * Generate subject-based correlation matrix (original implementation)
     */
    private double[][] generateSubjectBasedMatrix(List<Course> courses) {
        int n = courses.size();
        double[][] matrix = new double[n][n];

        // Group courses by their subject prefix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0.0; // Same course, no conflict with itself
                } else {
                    String courseName1 = courses.get(i).getName();
                    String courseName2 = courses.get(j).getName();

                    // Extract subject prefix (first 3 characters)
                    String prefix1 = courseName1.substring(0, Math.min(3, courseName1.length()));
                    String prefix2 = courseName2.substring(0, Math.min(3, courseName2.length()));

                    if (prefix1.equals(prefix2)) {
                        // Same program - hard constraint
                        matrix[i][j] = 2.0;
                    } else {
                        // Different programs - assign random correlation
                        double rand = random.nextDouble();
                        if (rand < 0.1) {
                            matrix[i][j] = 1.0; // High correlation (10%)
                        } else if (rand < 0.3) {
                            matrix[i][j] = 0.5; // Medium correlation (20%)
                        } else {
                            matrix[i][j] = 0.0; // No correlation (70%)
                        }
                    }
                }
            }
        }

        return matrix;
    }
}
