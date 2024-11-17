package com.roomallocation.util;

import com.roomallocation.model.Room;
import com.roomallocation.model.RoomType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RoomDataLoader {
    private static final String ROOMS_FILE = "/rooms.csv";

    public static List<Room> loadRooms() {
        System.out.println("Loading rooms from CSV file...");
        List<Room> rooms = new ArrayList<>();

        try (InputStream is = RoomDataLoader.class.getResourceAsStream(ROOMS_FILE)) {
            if (is == null) {
                throw new IOException("Could not find " + ROOMS_FILE + " in resources directory");
            }

            // Explicitly specify UTF-8 encoding for French characters
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                
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
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading rooms from CSV: " + e.getMessage(), e);
        }

        if (rooms.isEmpty()) {
            throw new RuntimeException("No rooms loaded from CSV file");
        }

        System.out.println("Loaded " + rooms.size() + " rooms");
        return rooms;
    }

//  public static void main(String[] args) {
//      List<Room> rooms = loadRooms();
//     for (Room room : rooms) {
//         System.out.println(room.toString());
//      }
//  }
}
