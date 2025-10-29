package com.simulator;

import com.roomallocation.model.Course;
import com.roomallocation.model.Professor;
import com.roomallocation.model.Room;
import com.roomallocation.model.RoomType;

import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Utility class to export simulation data to JSON format compatible with frontend
 */
public class JsonExporter {

    /**
     * Export all simulation data to JSON files
     */
    public static void exportAll(SimulationRunner.SimulationResult result, String outputDirectory) throws IOException {
        exportCourses(result.getCourses(), outputDirectory + "/courses.json");
        exportProfessors(result.getProfessors(), outputDirectory + "/professors.json");
        exportRooms(result.getRooms(), outputDirectory + "/rooms.json");
        exportCorrelationMatrix(result.getCorrelationMatrix(), result.getCourses(), outputDirectory + "/correlation_matrix.json");
        System.out.println("Exported all data to " + outputDirectory);
    }

    /**
     * Export courses to JSON
     */
    public static void exportCourses(List<Course> courses, String filepath) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            json.append("  {\n");
            json.append("    \"id\": \"").append(c.getName()).append("\",\n");
            json.append("    \"name\": \"").append(c.getName()).append("\",\n");
            json.append("    \"cohortSize\": ").append(c.getCohortSize()).append(",\n");
            json.append("    \"durationMinutes\": ").append(c.getDurationMinutes()).append(",\n");
            json.append("    \"professorId\": \"").append(c.getProfessorId()).append("\",\n");
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
        writeToFile(filepath, json.toString());
    }

    /**
     * Export professors to JSON
     */
    public static void exportProfessors(List<Professor> professors, String filepath) throws IOException {
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
        writeToFile(filepath, json.toString());
    }

    /**
     * Export rooms to JSON
     */
    public static void exportRooms(List<Room> rooms, String filepath) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            json.append("  {\n");
            json.append("    \"name\": \"").append(r.getName()).append("\",\n");
            json.append("    \"capacity\": ").append(r.getCapacity()).append(",\n");
            json.append("    \"type\": \"").append(r.getType().name()).append("\",\n");
            json.append("    \"building\": \"").append(getRoomTypeBuilding(r.getType())).append("\"");

            // Add unavailable slots if present
            if (r.getAllUnavailableSlots() != null && !r.getAllUnavailableSlots().isEmpty()) {
                json.append(",\n");
                json.append("    \"unavailableSlots\": ").append(formatUnavailableSlots(r));
            }

            json.append("\n  }");
            if (i < rooms.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]\n");
        writeToFile(filepath, json.toString());
    }

    /**
     * Export correlation matrix to JSON
     */
    public static void exportCorrelationMatrix(double[][] matrix, List<Course> courses, String filepath) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"courseNames\": [\n");

        // Write course names for reference
        for (int i = 0; i < courses.size(); i++) {
            json.append("    \"").append(courses.get(i).getName()).append("\"");
            if (i < courses.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");

        // Write matrix
        json.append("  \"matrix\": [\n");
        for (int i = 0; i < matrix.length; i++) {
            json.append("    [");
            for (int j = 0; j < matrix[i].length; j++) {
                json.append(matrix[i][j]);
                if (j < matrix[i].length - 1) {
                    json.append(", ");
                }
            }
            json.append("]");
            if (i < matrix.length - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ]\n");

        json.append("}\n");
        writeToFile(filepath, json.toString());
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

    /**
     * Format professor availability as JSON object
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
     * Format unavailable slots for a room
     */
    private static String formatUnavailableSlots(Room room) {
        Map<DayOfWeek, List<com.roomallocation.model.TimeSlot>> slots = room.getAllUnavailableSlots();
        if (slots == null || slots.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[\n");
        boolean first = true;

        for (Map.Entry<DayOfWeek, List<com.roomallocation.model.TimeSlot>> entry : slots.entrySet()) {
            for (com.roomallocation.model.TimeSlot slot : entry.getValue()) {
                if (!first) {
                    sb.append(",\n");
                }
                sb.append("      {\n");
                sb.append("        \"day\": \"").append(entry.getKey().name()).append("\",\n");
                sb.append("        \"startTime\": \"").append(formatTime(slot.getStartTime())).append("\",\n");
                sb.append("        \"endTime\": \"").append(formatTime(slot.getEndTime())).append("\"\n");
                sb.append("      }");
                first = false;
            }
        }

        sb.append("\n    ]");
        return sb.toString();
    }

    /**
     * Format LocalTime as HH:mm string
     */
    private static String formatTime(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    /**
     * Get building name for room type (for frontend display)
     */
    private static String getRoomTypeBuilding(RoomType type) {
        switch (type) {
            case COULOIR_VANNEAU:
                return "Couloir cour Vanneau";
            case COULOIR_SCOLARITE:
                return "Couloir de la scolarité";
            case COULOIR_LABOS:
                return "Couloir des labos";
            case SALLES_100:
                return "Bâtiment Salles 100";
            case AMPHI_COULOIR_BINETS:
                return "Amphi couloir binets";
            case SALLES_INFO:
                return "Bâtiment Informatique";
            case SALLES_LANGUES:
                return "Bâtiment Langues";
            case NOUVEAUX_AMPHIS:
                return "Nouveaux amphithéâtres";
            case GRANDS_AMPHIS:
                return "Grands amphithéâtres";
            case AMPHIS_80_100:
                return "Amphithéâtres 80-100";
            default:
                return "Unknown Building";
        }
    }

    /**
     * Write string content to file
     */
    private static void writeToFile(String filepath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(content);
        }
        System.out.println("Exported to: " + filepath);
    }
}
