package com.roomallocation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.roomallocation.model.Course;
import com.roomallocation.model.Professor;
import com.roomallocation.util.CourseDataLoader;
import com.roomallocation.util.ProfessorDataLoader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HTTP Controller for professor endpoints
 */
public class ProfessorController {

    private final ObjectMapper objectMapper;
    private final List<Professor> professors;
    private final List<Course> courses;

    public ProfessorController() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.professors = ProfessorDataLoader.loadProfessors();
        this.courses = CourseDataLoader.loadCourses();
    }

    /**
     * Handler for GET /api/professors/{id}
     * Returns professor data by ID
     */
    public HttpHandler getProfessorByIdHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    String path = exchange.getRequestURI().getPath();
                    String professorId = path.substring(path.lastIndexOf('/') + 1);

                    Professor professor = professors.stream()
                        .filter(p -> p.getId().equals(professorId))
                        .findFirst()
                        .orElse(null);

                    if (professor != null) {
                        // Add courses to professor object
                        List<Course> professorCourses = courses.stream()
                            .filter(c -> professorId.equals(c.getProfessorId()))
                            .collect(Collectors.toList());

                        Map<String, Object> response = new HashMap<>();
                        response.put("id", professor.getId());
                        response.put("name", professor.getName());
                        response.put("availability", professor.getAvailability());
                        response.put("courses", professorCourses);

                        String jsonResponse = objectMapper.writeValueAsString(response);
                        sendJsonResponse(exchange, 200, jsonResponse);
                    } else {
                        sendErrorResponse(exchange, 404, "Professor not found");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorResponse(exchange, 500, "Failed to fetch professor: " + e.getMessage());
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleCORS(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    /**
     * Handler for GET /api/professors/{id}/courses
     * Returns courses taught by a professor
     */
    public HttpHandler getCoursesByProfessorHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    String path = exchange.getRequestURI().getPath();
                    String pathWithoutCourses = path.substring(0, path.lastIndexOf('/'));
                    String professorId = pathWithoutCourses.substring(pathWithoutCourses.lastIndexOf('/') + 1);

                    List<Course> professorCourses = courses.stream()
                        .filter(c -> professorId.equals(c.getProfessorId()))
                        .collect(Collectors.toList());

                    String jsonResponse = objectMapper.writeValueAsString(professorCourses);
                    sendJsonResponse(exchange, 200, jsonResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorResponse(exchange, 500, "Failed to fetch courses: " + e.getMessage());
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleCORS(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    /**
     * Handler for POST /api/professors/preferences
     * Submit professor preferences and availability
     */
    public HttpHandler submitPreferencesHandler() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String requestBody = new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                    );

                    System.out.println("Received preferences submission:");
                    System.out.println(requestBody);

                    // In a real application, this would save to database
                    // For now, just acknowledge receipt
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Preferences saved successfully");

                    String jsonResponse = objectMapper.writeValueAsString(response);
                    sendJsonResponse(exchange, 200, jsonResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorResponse(exchange, 500, "Failed to save preferences: " + e.getMessage());
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleCORS(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    /**
     * Handler for PUT /api/professors/{id}/availability
     * Update professor availability
     */
    public HttpHandler updateAvailabilityHandler() {
        return exchange -> {
            if ("PUT".equals(exchange.getRequestMethod())) {
                try {
                    String requestBody = new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                    );

                    System.out.println("Received availability update:");
                    System.out.println(requestBody);

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Availability updated successfully");

                    String jsonResponse = objectMapper.writeValueAsString(response);
                    sendJsonResponse(exchange, 200, jsonResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorResponse(exchange, 500, "Failed to update availability: " + e.getMessage());
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleCORS(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    /**
     * Handler for GET /api/professors/{id}/allocation
     * Get allocation results for a professor
     */
    public HttpHandler getAllocationResultsHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    String path = exchange.getRequestURI().getPath();
                    String pathWithoutAllocation = path.substring(0, path.lastIndexOf('/'));
                    String professorId = pathWithoutAllocation.substring(pathWithoutAllocation.lastIndexOf('/') + 1);

                    Professor professor = professors.stream()
                        .filter(p -> p.getId().equals(professorId))
                        .findFirst()
                        .orElse(null);

                    if (professor != null) {
                        // For now, return PENDING status
                        // In a real application, this would check if allocation has been run
                        Map<String, Object> response = new HashMap<>();
                        response.put("professorId", professorId);
                        response.put("professorName", professor.getName());
                        response.put("status", "PENDING");
                        response.put("allocatedClasses", List.of());
                        response.put("unallocatedCourses", List.of());
                        // Set estimated publish date to 10 days from now
                        response.put("estimatedPublishDate",
                            java.time.Instant.now().plus(java.time.Duration.ofDays(10)).toString());

                        String jsonResponse = objectMapper.writeValueAsString(response);
                        sendJsonResponse(exchange, 200, jsonResponse);
                    } else {
                        sendErrorResponse(exchange, 404, "Professor not found");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorResponse(exchange, 500, "Failed to fetch allocation results: " + e.getMessage());
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleCORS(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    /**
     * Helper method to send JSON response
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
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
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
    }
}
