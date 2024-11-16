import java.io.*;
import java.util.*;
import java.nio.file.*;

public class RoomDataLoader {
    private static final String ROOMS_FILE = "rooms.csv";

    public static List<Room> loadRooms() {
        System.out.println("Loading rooms from CSV file...");
        List<Room> rooms = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(ROOMS_FILE))) {
            // Skip header line
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    int capacity = Integer.parseInt(parts[1].trim());
                    RoomType type = RoomType.fromString(parts[2].trim());

                    rooms.add(new Room(name, capacity, type));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading rooms from CSV: " + e.getMessage());
            // Fallback to hardcoded data if file not found
            // return getHardcodedRooms();
        }
        System.out.println("Loaded " + rooms.size() + " rooms");
        return rooms;
    }

    // public static void main(String[] args) {
    //     List<Room> rooms = loadRooms();
    //     for (Room room : rooms) {
    //         System.out.println(room.toString());
    //     }
    // }
}
