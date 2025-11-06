package com.roomallocation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomallocation.model.Course;
import com.roomallocation.model.Professor;
import com.roomallocation.model.Room;
import com.roomallocation.service.AdminService;
import com.roomallocation.util.CourseDataLoader;
import com.roomallocation.util.ProfessorDataLoader;
import com.roomallocation.util.RoomDataLoader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * HTTP Controller for admin endpoints
 */
public class AdminController {

    private final AdminService adminService;
    private final ObjectMapper objectMapper;
    private final List<Professor> professors;
    private final List<Course> courses;
    private final List<Room> rooms;

    public AdminController() {
        this.adminService = new AdminService();
        this.objectMapper = new ObjectMapper();
        this.professors = ProfessorDataLoader.loadProfessors();
        this.courses = CourseDataLoader.loadCourses();
        this.rooms = RoomDataLoader.loadRooms();
    }

    /**
     * Handler for GET /api/admin/statistics
     * Returns system-wide statistics
     */
    public HttpHandler getStatisticsHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    Map<String, Object> stats = adminService.getSystemStatistics(
                        professors, courses, rooms
                    );

                    String jsonResponse = objectMapper.writeValueAsString(stats);
                    sendJsonResponse(exchange, 200, jsonResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorResponse(exchange, 500, "Failed to fetch statistics: " + e.getMessage());
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleCORS(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    /**
     * Handler for GET /api/admin/preferences/status
     * Returns preference completion status for all professors
     */
    public HttpHandler getPreferenceStatusHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    Map<String, Object> prefStats = adminService.getPreferenceStatistics(
                        professors, courses
                    );

                    String jsonResponse = objectMapper.writeValueAsString(prefStats);
                    sendJsonResponse(exchange, 200, jsonResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorResponse(exchange, 500, "Failed to fetch preference status: " + e.getMessage());
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleCORS(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    /**
     * Handler for GET /api/admin/professors/incomplete
     * Returns list of professors who haven't completed preferences
     */
    public HttpHandler getProfessorsWithoutPreferencesHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    List<Map<String, Object>> profs = adminService.getProfessorsWithoutPreferences(
                        professors, courses
                    );

                    String jsonResponse = objectMapper.writeValueAsString(profs);
                    sendJsonResponse(exchange, 200, jsonResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorResponse(exchange, 500, "Failed to fetch incomplete professors: " + e.getMessage());
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleCORS(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    /**
     * Reload data from files (useful after updates)
     */
    public void reloadData() {
        System.out.println("Reloading data from files...");
        professors.clear();
        professors.addAll(ProfessorDataLoader.loadProfessors());

        courses.clear();
        courses.addAll(CourseDataLoader.loadCourses());

        rooms.clear();
        rooms.addAll(RoomDataLoader.loadRooms());

        System.out.println("Data reloaded successfully");
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
}
