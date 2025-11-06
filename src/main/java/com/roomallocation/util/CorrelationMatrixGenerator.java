package com.roomallocation.util;

import com.roomallocation.model.Course;
import java.util.*;

/**
 * Utility class to generate student correlation matrix
 * The correlation matrix represents historical overlap of enrolled students between courses
 */
public class CorrelationMatrixGenerator {

    /**
     * Generate a correlation matrix for courses
     * @param courses List of courses
     * @return NxN matrix where matrix[i][j] represents correlation between course i and j
     */
    public static double[][] generateCorrelationMatrix(List<Course> courses) {
        int n = courses.size();
        double[][] matrix = new double[n][n];
        Random random = new Random(42); // Fixed seed for reproducibility

        // Fill diagonal with 0 (a course doesn't overlap with itself)
        for (int i = 0; i < n; i++) {
            matrix[i][i] = 0.0;
        }

        // Generate correlations based on course characteristics
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Course course1 = courses.get(i);
                Course course2 = courses.get(j);

                double correlation = calculateCorrelation(course1, course2, random);

                // Matrix is symmetric
                matrix[i][j] = correlation;
                matrix[j][i] = correlation;
            }
        }

        return matrix;
    }

    /**
     * Calculate correlation between two courses based on their characteristics
     * Correlation values:
     * - 2.0 = Same program (hard constraint - cannot overlap)
     * - 1.0-1.9 = High overlap (significant student overlap)
     * - 0.5-0.9 = Medium overlap (some student overlap)
     * - 0.0-0.4 = Low/no overlap
     */
    private static double calculateCorrelation(Course course1, Course course2, Random random) {
        // Use course name patterns to determine if they're in the same program
        String name1 = course1.getName().toLowerCase();
        String name2 = course2.getName().toLowerCase();

        // Check if courses are from the same program (e.g., "CS101" and "CS201")
        String prefix1 = extractCoursePrefix(name1);
        String prefix2 = extractCoursePrefix(name2);

        if (prefix1 != null && prefix2 != null && prefix1.equals(prefix2)) {
            // Check level (e.g., 100-level vs 200-level)
            int level1 = extractCourseLevel(name1);
            int level2 = extractCourseLevel(name2);

            if (Math.abs(level1 - level2) <= 1) {
                // Same program, similar level -> high probability of being same program
                // 20% chance of being exactly same program (correlation = 2.0)
                if (random.nextDouble() < 0.2) {
                    return 2.0; // Hard constraint
                } else {
                    // High correlation
                    return 1.0 + random.nextDouble() * 0.9; // 1.0-1.9
                }
            } else {
                // Same program, different levels -> medium correlation
                return 0.5 + random.nextDouble() * 0.5; // 0.5-1.0
            }
        }

        // Check if similar cohort size (might indicate same year/program)
        int sizeDiff = Math.abs(course1.getCohortSize() - course2.getCohortSize());
        if (sizeDiff < 20) {
            // Similar sizes -> might have some overlap
            return random.nextDouble() * 0.8; // 0.0-0.8
        }

        // Otherwise, low correlation
        return random.nextDouble() * 0.3; // 0.0-0.3
    }

    /**
     * Extract course prefix (e.g., "CS" from "CS101" or "cs-101")
     */
    private static String extractCoursePrefix(String courseName) {
        // Try to extract letters at the start
        StringBuilder prefix = new StringBuilder();
        for (char c : courseName.toCharArray()) {
            if (Character.isLetter(c)) {
                prefix.append(c);
            } else if (prefix.length() > 0) {
                break; // Stop after first letter sequence
            }
        }

        return prefix.length() > 0 ? prefix.toString() : null;
    }

    /**
     * Extract course level (e.g., 1 from "CS101", 2 from "CS201")
     * Returns the first digit as the level, or 0 if no digits found
     */
    private static int extractCourseLevel(String courseName) {
        for (char c : courseName.toCharArray()) {
            if (Character.isDigit(c)) {
                return Character.getNumericValue(c);
            }
        }
        return 0;
    }

    /**
     * Generate a simple correlation matrix with mostly low correlations
     * and a few high correlation pairs for testing
     */
    public static double[][] generateSimpleTestMatrix(List<Course> courses) {
        int n = courses.size();
        double[][] matrix = new double[n][n];
        Random random = new Random(42);

        // Initialize with low random correlations
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0.0;
                } else {
                    matrix[i][j] = random.nextDouble() * 0.3; // Low correlation
                }
            }
        }

        // Add some high correlations for every 10th course pair
        for (int i = 0; i < n - 1; i += 10) {
            if (i + 1 < n) {
                // Some pairs in same program (correlation = 2.0)
                matrix[i][i + 1] = 2.0;
                matrix[i + 1][i] = 2.0;
            }

            if (i + 2 < n) {
                // Some pairs with high overlap
                double corr = 1.0 + random.nextDouble() * 0.5;
                matrix[i][i + 2] = corr;
                matrix[i + 2][i] = corr;
            }
        }

        return matrix;
    }

    /**
     * Print correlation matrix statistics
     */
    public static void printStatistics(double[][] matrix, List<Course> courses) {
        int n = matrix.length;
        int hardConstraints = 0;
        int highCorrelations = 0;
        int mediumCorrelations = 0;
        int lowCorrelations = 0;

        List<String> sameProgramPairs = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double corr = matrix[i][j];

                if (corr >= 2.0) {
                    hardConstraints++;
                    sameProgramPairs.add(courses.get(i).getName() + " <-> " + courses.get(j).getName());
                } else if (corr >= 1.0) {
                    highCorrelations++;
                } else if (corr >= 0.5) {
                    mediumCorrelations++;
                } else {
                    lowCorrelations++;
                }
            }
        }

        System.out.println("\nCorrelation Matrix Statistics:");
        System.out.println("  Total course pairs: " + (n * (n - 1) / 2));
        System.out.println("  Same program pairs (correlation = 2.0): " + hardConstraints);
        System.out.println("  High correlation pairs (1.0-1.9): " + highCorrelations);
        System.out.println("  Medium correlation pairs (0.5-0.9): " + mediumCorrelations);
        System.out.println("  Low correlation pairs (0.0-0.4): " + lowCorrelations);

        if (!sameProgramPairs.isEmpty() && sameProgramPairs.size() <= 20) {
            System.out.println("\n  Same program course pairs:");
            for (String pair : sameProgramPairs) {
                System.out.println("    - " + pair);
            }
        }
    }
}
