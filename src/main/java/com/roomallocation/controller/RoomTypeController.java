package com.roomallocation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomallocation.dto.RoomTypeInfoDTO;
import com.roomallocation.model.Room;
import com.roomallocation.model.RoomType;
import com.roomallocation.service.RoomTypeService;
import com.roomallocation.util.RoomDataLoader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * HTTP Controller for room type endpoints
 * Provides REST-like API for room type information
 */
public class RoomTypeController {

    private final RoomTypeService roomTypeService;
    private final ObjectMapper objectMapper;
    private List<Room> rooms;

    public RoomTypeController() {
        this.roomTypeService = new RoomTypeService();
        this.objectMapper = new ObjectMapper();
        this.rooms = RoomDataLoader.loadRooms();
    }

    /**
     * Handler for GET /api/rooms/types
     * Returns all room type information
     */
    public HttpHandler getAllRoomTypesHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    List<RoomTypeInfoDTO> roomTypes = roomTypeService.aggregateRoomsByType(rooms);
                    String jsonResponse = objectMapper.writeValueAsString(roomTypes);

                    sendJsonResponse(exchange, 200, jsonResponse);
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, "Failed to fetch room types: " + e.getMessage());
                }
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    /**
     * Handler for GET /api/rooms/type/{roomType}
     * Returns information for a specific room type
     */
    public HttpHandler getRoomTypeHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    // Extract room type from path
                    String path = exchange.getRequestURI().getPath();
                    String[] pathParts = path.split("/");
                    String roomTypeStr = pathParts[pathParts.length - 1];

                    RoomType roomType = RoomType.valueOf(roomTypeStr);
                    RoomTypeInfoDTO roomTypeInfo = roomTypeService.getRoomTypeInfo(roomType, rooms);

                    if (roomTypeInfo != null) {
                        String jsonResponse = objectMapper.writeValueAsString(roomTypeInfo);
                        sendJsonResponse(exchange, 200, jsonResponse);
                    } else {
                        sendErrorResponse(exchange, 404, "Room type not found");
                    }
                } catch (IllegalArgumentException e) {
                    sendErrorResponse(exchange, 400, "Invalid room type");
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, "Failed to fetch room type: " + e.getMessage());
                }
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
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
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
}
