package com.roomallocation.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.roomallocation.model.Professor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

/**
 * Utility class to load professor data from JSON files
 */
public class ProfessorDataLoader {
    private static final String PROFESSORS_FILE = "/professors.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Load professors from the JSON file
     */
    public static List<Professor> loadProfessors() {
        System.out.println("Loading professors from JSON file...");
        List<Professor> professors = new ArrayList<>();

        try (InputStream is = ProfessorDataLoader.class.getResourceAsStream(PROFESSORS_FILE)) {
            if (is == null) {
                throw new IOException("Could not find " + PROFESSORS_FILE + " in resources directory");
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {

                JsonNode rootNode = mapper.readTree(reader);

                for (JsonNode professorNode : rootNode) {
                    String id = professorNode.get("id").asText();
                    String name = professorNode.get("name").asText();

                    Professor professor = new Professor(id, name);

                    // Load availability if present
                    if (professorNode.has("availability")) {
                        JsonNode availNode = professorNode.get("availability");
                        loadAvailability(professor, availNode);
                    }

                    professors.add(professor);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading professors from JSON: " + e.getMessage(), e);
        }

        if (professors.isEmpty()) {
            throw new RuntimeException("No professors loaded from JSON file");
        }

        System.out.println("Loaded " + professors.size() + " professors");
        return professors;
    }

    /**
     * Load availability data for a professor
     */
    private static void loadAvailability(Professor professor, JsonNode availNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = availNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String dayStr = entry.getKey();
            JsonNode periods = entry.getValue();

            try {
                DayOfWeek day = DayOfWeek.valueOf(dayStr.toUpperCase());

                // Clear default availability for this day first
                professor.removeAvailability(day);

                // Add each availability period
                for (JsonNode periodNode : periods) {
                    String startTimeStr = periodNode.get("startTime").asText();
                    String endTimeStr = periodNode.get("endTime").asText();

                    LocalTime startTime = parseTime(startTimeStr);
                    LocalTime endTime = parseTime(endTimeStr);

                    professor.addAvailabilityPeriod(day, startTime, endTime);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid day of week: " + dayStr);
            }
        }
    }

    /**
     * Parse time string in HH:mm format
     */
    private static LocalTime parseTime(String timeStr) {
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return LocalTime.of(hour, minute);
    }

    /**
     * Save professors to JSON file (for future updates)
     */
    public static void saveProfessors(List<Professor> professors, String filepath) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < professors.size(); i++) {
            Professor p = professors.get(i);
            json.append("  {\n");
            json.append("    \"id\": \"").append(p.getId()).append("\",\n");
            json.append("    \"name\": \"").append(p.getName()).append("\",\n");
            json.append("    \"availability\": ").append(formatAvailability(p.getAvailability()));
            json.append("\n  }");

            if (i < professors.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]\n");

        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(json.toString());
        }
        System.out.println("Saved professors to: " + filepath);
    }

    /**
     * Format availability as JSON
     */
    private static String formatAvailability(Map<DayOfWeek, List<Professor.AvailabilityPeriod>> availability) {
        StringBuilder sb = new StringBuilder("{\n");

        DayOfWeek[] days = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                           DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

        boolean first = true;
        for (DayOfWeek day : days) {
            List<Professor.AvailabilityPeriod> periods = availability.get(day);
            if (periods != null && !periods.isEmpty()) {
                if (!first) {
                    sb.append(",\n");
                }
                sb.append("      \"").append(day.name()).append("\": [\n");

                for (int i = 0; i < periods.size(); i++) {
                    Professor.AvailabilityPeriod period = periods.get(i);
                    sb.append("        {\n");
                    sb.append("          \"startTime\": \"").append(formatTime(period.getStartTime())).append("\",\n");
                    sb.append("          \"endTime\": \"").append(formatTime(period.getEndTime())).append("\"\n");
                    sb.append("        }");
                    if (i < periods.size() - 1) {
                        sb.append(",");
                    }
                    sb.append("\n");
                }

                sb.append("      ]");
                first = false;
            }
        }

        sb.append("\n    }");
        return sb.toString();
    }

    /**
     * Format LocalTime as HH:mm string
     */
    private static String formatTime(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
}
