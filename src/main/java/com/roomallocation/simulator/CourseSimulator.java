package com.roomallocation.simulator;

import com.roomallocation.model.Course;
import com.roomallocation.strategy.PreferenceGenerationStrategy;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class CourseSimulator {
    private final PreferenceGenerationStrategy strategy;
    private final Random random;

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
            courses.get(i).setTypePreferences(strategy.generatePreferences(courses.get(i)));
        }
        return courses;
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
        int n = courses.size();
        double[][] correlationMatrix = new double[n][n];

        // Group courses by their subject prefix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    correlationMatrix[i][j] = 0.0; // Same course, no conflict with itself
                } else {
                    String courseName1 = courses.get(i).getName();
                    String courseName2 = courses.get(j).getName();

                    // Extract subject prefix (first 3 characters)
                    String prefix1 = courseName1.substring(0, Math.min(3, courseName1.length()));
                    String prefix2 = courseName2.substring(0, Math.min(3, courseName2.length()));

                    if (prefix1.equals(prefix2)) {
                        // Same program - hard constraint
                        correlationMatrix[i][j] = 2.0;
                    } else {
                        // Different programs - assign random correlation
                        double rand = random.nextDouble();
                        if (rand < 0.1) {
                            correlationMatrix[i][j] = 1.0; // High correlation (10%)
                        } else if (rand < 0.3) {
                            correlationMatrix[i][j] = 0.5; // Medium correlation (20%)
                        } else {
                            correlationMatrix[i][j] = 0.0; // No correlation (70%)
                        }
                    }
                }
            }
        }

        return correlationMatrix;
    }
}
