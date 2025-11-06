package com.roomallocation.server;

import com.roomallocation.controller.AdminController;
import com.roomallocation.controller.AlgorithmController;
import com.roomallocation.controller.RoomTypeController;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Simple HTTP server for the admin API
 * Provides REST endpoints for the admin dashboard
 */
public class ApiServer {

    private final HttpServer server;
    private final int port;
    private final AdminController adminController;
    private final AlgorithmController algorithmController;
    private final RoomTypeController roomTypeController;

    public ApiServer(int port) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        // Initialize controllers
        this.adminController = new AdminController();
        this.algorithmController = new AlgorithmController();
        this.roomTypeController = new RoomTypeController();

        // Register routes
        registerRoutes();

        // Set executor for handling requests
        server.setExecutor(Executors.newFixedThreadPool(10));
    }

    private void registerRoutes() {
        // Admin statistics endpoints
        server.createContext("/api/admin/statistics", adminController.getStatisticsHandler());
        server.createContext("/api/admin/preferences/status", adminController.getPreferenceStatusHandler());
        server.createContext("/api/admin/professors/incomplete", adminController.getProfessorsWithoutPreferencesHandler());

        // Algorithm endpoints
        server.createContext("/api/admin/algorithm/run", algorithmController.runAlgorithmHandler());
        server.createContext("/api/admin/algorithm/status", algorithmController.getStatusHandler());

        // Room type endpoints
        server.createContext("/api/rooms/types", roomTypeController.getAllRoomTypesHandler());
        server.createContext("/api/rooms/type/", roomTypeController.getRoomTypeHandler());

        System.out.println("Registered API routes:");
        System.out.println("  - GET  /api/admin/statistics");
        System.out.println("  - GET  /api/admin/preferences/status");
        System.out.println("  - GET  /api/admin/professors/incomplete");
        System.out.println("  - POST /api/admin/algorithm/run");
        System.out.println("  - GET  /api/admin/algorithm/status");
        System.out.println("  - GET  /api/rooms/types");
        System.out.println("  - GET  /api/rooms/type/{roomType}");
    }

    /**
     * Start the server
     */
    public void start() {
        server.start();
        System.out.println("\n========================================");
        System.out.println("API Server started on port " + port);
        System.out.println("Base URL: http://localhost:" + port);
        System.out.println("========================================\n");
    }

    /**
     * Stop the server
     */
    public void stop() {
        System.out.println("Stopping API server...");
        algorithmController.shutdown();
        server.stop(0);
        System.out.println("API server stopped");
    }

    /**
     * Stop the server after a delay
     */
    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
        algorithmController.shutdown();
    }

    /**
     * Get the server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Main method to run the server standalone
     */
    public static void main(String[] args) {
        try {
            int port = 8080;

            // Check for port argument
            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port number. Using default port 8080");
                }
            }

            ApiServer server = new ApiServer(port);
            server.start();

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutdown signal received");
                server.stop();
            }));

            System.out.println("Press Ctrl+C to stop the server");

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
