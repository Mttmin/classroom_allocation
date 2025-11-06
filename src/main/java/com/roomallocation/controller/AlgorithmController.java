package com.roomallocation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomallocation.allocation.TypeBasedAllocation;
import com.roomallocation.model.Course;
import com.roomallocation.model.Room;
import com.roomallocation.strategy.*;
import com.roomallocation.util.CourseDataLoader;
import com.roomallocation.util.PreferenceCompletionUtil;
import com.roomallocation.util.RoomDataLoader;
import com.simulator.CourseSimulator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP Controller for algorithm execution
 */
public class AlgorithmController {

    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private volatile boolean isRunning = false;
    private volatile Map<String, Object> lastResult = null;

    public AlgorithmController() {
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Handler for POST /api/admin/algorithm/run
     * Runs the allocation algorithm with specified parameters
     */
    public HttpHandler runAlgorithmHandler() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    // Parse request body
                    String requestBody = new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                    );

                    @SuppressWarnings("unchecked")
                    Map<String, Object> params = objectMapper.readValue(
                        requestBody,
                        Map.class
                    );

                    // Extract parameters
                    String strategyType = (String) params.getOrDefault("strategy", "SmartRandom");
                    int numPreferences = (int) params.getOrDefault("numPreferences", 10);
                    boolean useExistingCourses = (boolean) params.getOrDefault("useExistingCourses", true);
                    boolean completePreferences = (boolean) params.getOrDefault("completePreferences", true);

                    // Simulation parameters (if generating new courses)
                    int numCourses = (int) params.getOrDefault("numCourses", 70);
                    int minSize = (int) params.getOrDefault("minSize", 10);
                    int maxSize = (int) params.getOrDefault("maxSize", 200);
                    int changeSize = (int) params.getOrDefault("changeSize", 35);

                    if (isRunning) {
                        sendErrorResponse(exchange, 409, "Algorithm is already running");
                        return;
                    }

                    isRunning = true;

                    // Run algorithm in background
                    executorService.submit(() -> {
                        try {
                            runAllocation(
                                strategyType, numPreferences, useExistingCourses,
                                completePreferences, numCourses, minSize, maxSize, changeSize
                            );
                        } finally {
                            isRunning = false;
                        }
                    });

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Algorithm started");

                    String jsonResponse = objectMapper.writeValueAsString(response);
                    sendJsonResponse(exchange, 200, jsonResponse);

                } catch (Exception e) {
                    isRunning = false;
                    e.printStackTrace();
                    sendErrorResponse(exchange, 500, "Failed to start algorithm: " + e.getMessage());
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleCORS(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    /**
     * Handler for GET /api/admin/algorithm/status
     * Returns current algorithm status and last result
     */
    public HttpHandler getStatusHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    Map<String, Object> status = new HashMap<>();
                    status.put("isRunning", isRunning);
                    status.put("lastResult", lastResult);

                    String jsonResponse = objectMapper.writeValueAsString(status);
                    sendJsonResponse(exchange, 200, jsonResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorResponse(exchange, 500, "Failed to fetch status: " + e.getMessage());
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleCORS(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    /**
     * Run the allocation algorithm
     */
    private void runAllocation(
            String strategyType,
            int numPreferences,
            boolean useExistingCourses,
            boolean completePreferences,
            int numCourses,
            int minSize,
            int maxSize,
            int changeSize) {

        try {
            System.out.println("===== Starting Allocation Algorithm =====");
            System.out.println("Strategy: " + strategyType);
            System.out.println("Number of preferences: " + numPreferences);
            System.out.println("Use existing courses: " + useExistingCourses);
            System.out.println("Complete preferences: " + completePreferences);

            // Load rooms
            List<Room> rooms = RoomDataLoader.loadRooms();
            System.out.println("Loaded " + rooms.size() + " rooms");

            // Get courses
            List<Course> courses;
            if (useExistingCourses) {
                courses = CourseDataLoader.loadCourses();
                System.out.println("Loaded " + courses.size() + " existing courses");
            } else {
                // Generate new courses
                System.out.println("Generating " + numCourses + " courses...");
                PreferenceGenerationStrategy strategy = createStrategy(strategyType, numPreferences, rooms);
                CourseSimulator simulator = new CourseSimulator(strategy);
                courses = simulator.generateCourses(numCourses, minSize, maxSize, changeSize);
                System.out.println("Generated " + courses.size() + " courses");
            }

            // Complete preferences if needed
            if (completePreferences) {
                System.out.println("Completing missing preferences...");
                PreferenceCompletionUtil.completeAllCoursePreferences(courses, rooms);
                System.out.println("Preferences completed");
            }

            // Run allocation
            System.out.println("Running allocation algorithm...");
            TypeBasedAllocation allocator = new TypeBasedAllocation(courses, rooms);
            Map<String, String> assignments = allocator.allocate();

            System.out.println("Allocation complete!");
            System.out.println("Assigned " + assignments.size() + " courses");

            // Prepare result
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("totalCourses", courses.size());
            result.put("assignedCourses", assignments.size());
            result.put("unassignedCourses", courses.size() - assignments.size());
            result.put("assignments", assignments);
            result.put("allocationState", allocator.exportAllocationState());
            result.put("timestamp", System.currentTimeMillis());

            // Calculate statistics
            int firstChoiceCount = 0;
            int topThreeChoiceCount = 0;
            double totalChoiceNumber = 0;

            for (Course course : courses) {
                if (course.getAssignedRoom() != null) {
                    int choice = course.getChoiceNumber() + 1;
                    totalChoiceNumber += choice;

                    if (choice == 1) firstChoiceCount++;
                    if (choice <= 3) topThreeChoiceCount++;
                }
            }

            result.put("firstChoiceCount", firstChoiceCount);
            result.put("topThreeChoiceCount", topThreeChoiceCount);
            result.put("averageChoiceRank",
                assignments.size() > 0 ? totalChoiceNumber / assignments.size() : 0);
            result.put("allocationRate",
                courses.size() > 0 ? (double) assignments.size() / courses.size() : 0);

            lastResult = result;

            System.out.println("===== Allocation Algorithm Complete =====");
            System.out.println("First choice matches: " + firstChoiceCount);
            System.out.println("Top-3 choice matches: " + topThreeChoiceCount);
            System.out.println("Average choice rank: " + result.get("averageChoiceRank"));
            System.out.println("Allocation rate: " + result.get("allocationRate"));

        } catch (Exception e) {
            System.err.println("Error running allocation: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            errorResult.put("timestamp", System.currentTimeMillis());
            lastResult = errorResult;
        }
    }

    /**
     * Create preference generation strategy based on type
     */
    private PreferenceGenerationStrategy createStrategy(String strategyType, int numPreferences, List<Room> rooms) {
        return switch (strategyType.toLowerCase()) {
            case "satisfaction", "satisfactionbased" ->
                new SatisfactionBasedStrategy(numPreferences, rooms);
            case "sizebased", "size" ->
                new SizedBasedPreferenceStrategy(numPreferences, rooms);
            case "random" ->
                new RandomPreferenceStrategy(numPreferences);
            case "fixed" ->
                new FixedPreference(numPreferences);
            default -> // "smartrandom" or default
                new SmartRandomPreferenceStrategy(numPreferences, rooms);
        };
    }

    /**
     * Helper method to send JSON response
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Helper method to send error response
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String errorJson = String.format("{\"success\": false, \"error\": \"%s\"}", message);
        sendJsonResponse(exchange, statusCode, errorJson);
    }

    /**
     * Handle CORS preflight requests
     */
    private void handleCORS(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
    }

    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
