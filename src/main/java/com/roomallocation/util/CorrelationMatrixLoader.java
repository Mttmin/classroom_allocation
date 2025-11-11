package com.roomallocation.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomallocation.model.Course;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility class to load correlation matrix from JSON file
 */
public class CorrelationMatrixLoader {
    private static final String CORRELATION_FILE = "/correlation_matrix.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Load correlation matrix from JSON file
     * @param courses List of courses (order matters for indexing)
     * @return Correlation matrix aligned with course list
     */
    public static double[][] loadCorrelationMatrix(List<Course> courses) {
        System.out.println("Loading correlation matrix from JSON file...");

        try (InputStream is = CorrelationMatrixLoader.class.getResourceAsStream(CORRELATION_FILE)) {
            if (is == null) {
                System.out.println("Correlation matrix file not found, generating synthetic matrix...");
                return CorrelationMatrixGenerator.generateCorrelationMatrix(courses);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {

                JsonNode rootNode = mapper.readTree(reader);

                // Read course names from JSON
                List<String> jsonCourseNames = new ArrayList<>();
                JsonNode courseNamesNode = rootNode.get("courseNames");
                if (courseNamesNode != null && courseNamesNode.isArray()) {
                    for (JsonNode nameNode : courseNamesNode) {
                        jsonCourseNames.add(nameNode.asText());
                    }
                }

                // Build index mapping from course name to index
                Map<String, Integer> jsonIndexMap = new HashMap<>();
                for (int i = 0; i < jsonCourseNames.size(); i++) {
                    jsonIndexMap.put(jsonCourseNames.get(i), i);
                }

                // Read correlation matrix from JSON
                JsonNode matrixNode = rootNode.get("matrix");
                double[][] jsonMatrix = null;
                if (matrixNode != null && matrixNode.isArray()) {
                    int size = matrixNode.size();
                    jsonMatrix = new double[size][size];

                    for (int i = 0; i < size; i++) {
                        JsonNode rowNode = matrixNode.get(i);
                        if (rowNode.isArray()) {
                            for (int j = 0; j < rowNode.size() && j < size; j++) {
                                jsonMatrix[i][j] = rowNode.get(j).asDouble(0.0);
                            }
                        }
                    }
                }

                if (jsonMatrix == null) {
                    System.out.println("Invalid correlation matrix format, generating synthetic matrix...");
                    return CorrelationMatrixGenerator.generateCorrelationMatrix(courses);
                }

                // Create result matrix aligned with input courses
                int n = courses.size();
                double[][] resultMatrix = new double[n][n];

                // Initialize with zeros
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        resultMatrix[i][j] = 0.0;
                    }
                }

                // Map correlations from JSON to result matrix
                int matchedCourses = 0;
                for (int i = 0; i < n; i++) {
                    String courseName = courses.get(i).getName();
                    Integer jsonIndex = jsonIndexMap.get(courseName);

                    if (jsonIndex != null) {
                        matchedCourses++;
                        for (int j = 0; j < n; j++) {
                            String otherCourseName = courses.get(j).getName();
                            Integer otherJsonIndex = jsonIndexMap.get(otherCourseName);

                            if (otherJsonIndex != null) {
                                resultMatrix[i][j] = jsonMatrix[jsonIndex][otherJsonIndex];
                            }
                        }
                    }
                }

                System.out.println("Loaded correlation matrix for " + matchedCourses + "/" + n + " courses");

                // For courses not in the JSON, generate correlations
                if (matchedCourses < n) {
                    System.out.println("Generating correlations for " + (n - matchedCourses) + " unmatched courses...");
                    Random random = new Random(42);

                    for (int i = 0; i < n; i++) {
                        String courseName = courses.get(i).getName();
                        Integer jsonIndex = jsonIndexMap.get(courseName);

                        if (jsonIndex == null) {
                            // Generate correlations for this course
                            for (int j = 0; j < n; j++) {
                                if (i != j) {
                                    resultMatrix[i][j] = random.nextDouble() * 0.5; // Low random correlation
                                    resultMatrix[j][i] = resultMatrix[i][j];
                                }
                            }
                        }
                    }
                }

                return resultMatrix;
            }

        } catch (IOException e) {
            System.err.println("Error loading correlation matrix: " + e.getMessage());
            System.out.println("Falling back to generated correlation matrix...");
            return CorrelationMatrixGenerator.generateCorrelationMatrix(courses);
        }
    }

    /**
     * Load or generate correlation matrix with fallback
     */
    public static double[][] loadOrGenerateMatrix(List<Course> courses) {
        try {
            return loadCorrelationMatrix(courses);
        } catch (Exception e) {
            System.err.println("Failed to load correlation matrix: " + e.getMessage());
            System.out.println("Generating synthetic correlation matrix...");
            return CorrelationMatrixGenerator.generateCorrelationMatrix(courses);
        }
    }
}
