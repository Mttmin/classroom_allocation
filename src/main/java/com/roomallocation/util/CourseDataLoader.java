package com.roomallocation.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.roomallocation.model.Course;
import com.roomallocation.model.RoomType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility class to load course data from JSON files
 */
public class CourseDataLoader {
    private static final String COURSES_FILE = "/courses.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Load courses from the JSON file
     */
    public static List<Course> loadCourses() {
        System.out.println("Loading courses from JSON file...");
        List<Course> courses = new ArrayList<>();

        try (InputStream is = CourseDataLoader.class.getResourceAsStream(COURSES_FILE)) {
            if (is == null) {
                System.out.println("No courses.json file found in resources directory");
                return courses; // Return empty list, not an error
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {

                JsonNode rootNode = mapper.readTree(reader);

                for (JsonNode courseNode : rootNode) {
                    String name = courseNode.get("name").asText();
                    int cohortSize = courseNode.get("cohortSize").asInt();
                    int durationMinutes = courseNode.has("durationMinutes")
                        ? courseNode.get("durationMinutes").asInt()
                        : 60;
                    
                    List<String> professorIds = new ArrayList<>();
                    if (courseNode.has("professorIds")) {
                        for (JsonNode idNode : courseNode.get("professorIds")) {
                            professorIds.add(idNode.asText());
                        }
                    } else if (courseNode.has("professorId")) {
                        professorIds.add(courseNode.get("professorId").asText());
                    }

                    Course course = new Course(name, cohortSize, durationMinutes, professorIds);

                    // Load type preferences if present
                    if (courseNode.has("typePreferences")) {
                        JsonNode prefsNode = courseNode.get("typePreferences");
                        List<RoomType> preferences = new ArrayList<>();

                        for (JsonNode prefNode : prefsNode) {
                            try {
                                RoomType type = RoomType.valueOf(prefNode.asText());
                                preferences.add(type);
                            } catch (IllegalArgumentException e) {
                                System.err.println("Invalid room type: " + prefNode.asText());
                            }
                        }

                        course.setTypePreferences(preferences);
                    }

                    // Load assigned room if present
                    if (courseNode.has("assignedRoom")) {
                        course.setAssignedRoom(courseNode.get("assignedRoom").asText());
                    }

                    courses.add(course);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading courses from JSON: " + e.getMessage());
            return courses; // Return what we have so far
        }

        System.out.println("Loaded " + courses.size() + " courses");
        return courses;
    }

    /**
     * Save courses to JSON file
     */
    public static void saveCourses(List<Course> courses, String filepath) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            json.append("  {\n");
            json.append("    \"id\": \"").append(c.getName()).append("\",\n");
            json.append("    \"name\": \"").append(c.getName()).append("\",\n");
            json.append("    \"cohortSize\": ").append(c.getCohortSize()).append(",\n");
            json.append("    \"durationMinutes\": ").append(c.getDurationMinutes()).append(",\n");
            json.append("    \"professorIds\": ").append(formatStringList(c.getProfessorIds())).append(",\n");
            json.append("    \"typePreferences\": ").append(formatRoomTypeList(c.getTypePreferences()));

            if (c.getAssignedRoom() != null) {
                json.append(",\n");
                json.append("    \"assignedRoom\": \"").append(c.getAssignedRoom()).append("\"");
            }

            json.append("\n  }");
            if (i < courses.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]\n");

        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(json.toString());
        }
        System.out.println("Saved courses to: " + filepath);
    }

    /**
     * Format string list as JSON array
     */
    private static String formatStringList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i)).append("\"");
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Format room type list as JSON array
     */
    private static String formatRoomTypeList(List<RoomType> types) {
        if (types == null || types.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < types.size(); i++) {
            sb.append("\"").append(types.get(i).name()).append("\"");
            if (i < types.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
