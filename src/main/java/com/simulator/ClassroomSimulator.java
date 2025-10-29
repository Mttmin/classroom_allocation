package com.simulator;

import com.roomallocation.model.Room;
import com.roomallocation.model.RoomType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

public class ClassroomSimulator {
    private final Random random;

    /**
     * Distribution mode for generating classrooms
     */
    public enum DistributionMode {
        UNIFORM,           // Equal distribution across all room types
        REALISTIC,         // Realistic distribution based on typical university
        SMALL_FOCUSED,     // More small classrooms, fewer large ones
        LARGE_FOCUSED,     // More large classrooms/amphitheaters
        CUSTOM            // Custom distribution
    }

    /**
     * Configuration for room type distribution
     */
    public static class RoomTypeConfig {
        public RoomType type;
        public int count;
        public int minCapacity;
        public int maxCapacity;
        public double unavailabilityRate; // 0.0 to 1.0 (percentage of time unavailable)

        public RoomTypeConfig(RoomType type, int count, int minCapacity, int maxCapacity) {
            this(type, count, minCapacity, maxCapacity, 0.0);
        }

        public RoomTypeConfig(RoomType type, int count, int minCapacity, int maxCapacity, double unavailabilityRate) {
            this.type = type;
            this.count = count;
            this.minCapacity = minCapacity;
            this.maxCapacity = maxCapacity;
            this.unavailabilityRate = unavailabilityRate;
        }
    }

    public ClassroomSimulator() {
        this.random = new Random();
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    /**
     * Generate classrooms with a predefined distribution mode
     */
    public List<Room> generateClassrooms(int totalRooms, DistributionMode mode) {
        List<RoomTypeConfig> configs = getDistributionConfig(mode, totalRooms);
        return generateClassroomsFromConfig(configs);
    }

    /**
     * Generate classrooms with custom configuration
     */
    public List<Room> generateClassroomsFromConfig(List<RoomTypeConfig> configs) {
        List<Room> rooms = new ArrayList<>();
        Map<RoomType, Integer> counters = new HashMap<>();

        for (RoomTypeConfig config : configs) {
            counters.put(config.type, counters.getOrDefault(config.type, 0));

            for (int i = 0; i < config.count; i++) {
                int counter = counters.get(config.type) + 1;
                counters.put(config.type, counter);

                // Generate room name
                String roomName = generateRoomName(config.type, counter);

                // Generate capacity within range
                int capacity = config.minCapacity +
                    random.nextInt(config.maxCapacity - config.minCapacity + 1);

                // Create room
                Room room = new Room(roomName, capacity, config.type);

                // Add unavailable slots if configured
                if (config.unavailabilityRate > 0) {
                    addRandomUnavailableSlots(room, config.unavailabilityRate);
                }

                rooms.add(room);
            }
        }

        return rooms;
    }

    /**
     * Get predefined distribution configuration based on mode
     */
    private List<RoomTypeConfig> getDistributionConfig(DistributionMode mode, int totalRooms) {
        List<RoomTypeConfig> configs = new ArrayList<>();

        switch (mode) {
            case UNIFORM:
                // Equal distribution across all types
                int roomsPerType = totalRooms / RoomType.values().length;
                for (RoomType type : RoomType.values()) {
                    configs.add(createConfigForType(type, roomsPerType));
                }
                break;

            case REALISTIC:
                // Realistic university distribution
                configs.add(new RoomTypeConfig(RoomType.SALLES_100, (int)(totalRooms * 0.15), 20, 40, 0.05));
                configs.add(new RoomTypeConfig(RoomType.COULOIR_VANNEAU, (int)(totalRooms * 0.12), 15, 35, 0.03));
                configs.add(new RoomTypeConfig(RoomType.COULOIR_SCOLARITE, (int)(totalRooms * 0.10), 15, 30, 0.02));
                configs.add(new RoomTypeConfig(RoomType.COULOIR_LABOS, (int)(totalRooms * 0.08), 12, 25, 0.10));
                configs.add(new RoomTypeConfig(RoomType.SALLES_INFO, (int)(totalRooms * 0.15), 20, 40, 0.15));
                configs.add(new RoomTypeConfig(RoomType.SALLES_LANGUES, (int)(totalRooms * 0.10), 15, 25, 0.08));
                configs.add(new RoomTypeConfig(RoomType.NOUVEAUX_AMPHIS, (int)(totalRooms * 0.10), 60, 120, 0.05));
                configs.add(new RoomTypeConfig(RoomType.AMPHI_COULOIR_BINETS, (int)(totalRooms * 0.08), 50, 100, 0.04));
                configs.add(new RoomTypeConfig(RoomType.AMPHIS_80_100, (int)(totalRooms * 0.07), 80, 100, 0.06));
                configs.add(new RoomTypeConfig(RoomType.GRANDS_AMPHIS, (int)(totalRooms * 0.05), 150, 400, 0.08));
                break;

            case SMALL_FOCUSED:
                // More small classrooms
                configs.add(new RoomTypeConfig(RoomType.SALLES_100, (int)(totalRooms * 0.25), 20, 40));
                configs.add(new RoomTypeConfig(RoomType.COULOIR_VANNEAU, (int)(totalRooms * 0.20), 15, 35));
                configs.add(new RoomTypeConfig(RoomType.COULOIR_SCOLARITE, (int)(totalRooms * 0.15), 15, 30));
                configs.add(new RoomTypeConfig(RoomType.COULOIR_LABOS, (int)(totalRooms * 0.10), 12, 25));
                configs.add(new RoomTypeConfig(RoomType.SALLES_INFO, (int)(totalRooms * 0.10), 20, 40));
                configs.add(new RoomTypeConfig(RoomType.SALLES_LANGUES, (int)(totalRooms * 0.10), 15, 25));
                configs.add(new RoomTypeConfig(RoomType.NOUVEAUX_AMPHIS, (int)(totalRooms * 0.05), 60, 120));
                configs.add(new RoomTypeConfig(RoomType.AMPHI_COULOIR_BINETS, (int)(totalRooms * 0.03), 50, 100));
                configs.add(new RoomTypeConfig(RoomType.AMPHIS_80_100, (int)(totalRooms * 0.01), 80, 100));
                configs.add(new RoomTypeConfig(RoomType.GRANDS_AMPHIS, (int)(totalRooms * 0.01), 150, 400));
                break;

            case LARGE_FOCUSED:
                // More large amphitheaters
                configs.add(new RoomTypeConfig(RoomType.GRANDS_AMPHIS, (int)(totalRooms * 0.15), 150, 400));
                configs.add(new RoomTypeConfig(RoomType.AMPHIS_80_100, (int)(totalRooms * 0.15), 80, 100));
                configs.add(new RoomTypeConfig(RoomType.NOUVEAUX_AMPHIS, (int)(totalRooms * 0.20), 60, 120));
                configs.add(new RoomTypeConfig(RoomType.AMPHI_COULOIR_BINETS, (int)(totalRooms * 0.15), 50, 100));
                configs.add(new RoomTypeConfig(RoomType.SALLES_100, (int)(totalRooms * 0.10), 20, 40));
                configs.add(new RoomTypeConfig(RoomType.COULOIR_VANNEAU, (int)(totalRooms * 0.08), 15, 35));
                configs.add(new RoomTypeConfig(RoomType.COULOIR_SCOLARITE, (int)(totalRooms * 0.07), 15, 30));
                configs.add(new RoomTypeConfig(RoomType.COULOIR_LABOS, (int)(totalRooms * 0.05), 12, 25));
                configs.add(new RoomTypeConfig(RoomType.SALLES_INFO, (int)(totalRooms * 0.03), 20, 40));
                configs.add(new RoomTypeConfig(RoomType.SALLES_LANGUES, (int)(totalRooms * 0.02), 15, 25));
                break;

            case CUSTOM:
                // Will be configured manually
                break;
        }

        return configs;
    }

    /**
     * Create a default config for a room type
     */
    private RoomTypeConfig createConfigForType(RoomType type, int count) {
        // Default capacity ranges based on room type
        switch (type) {
            case COULOIR_VANNEAU:
            case COULOIR_SCOLARITE:
                return new RoomTypeConfig(type, count, 15, 35);
            case COULOIR_LABOS:
                return new RoomTypeConfig(type, count, 12, 25);
            case SALLES_100:
                return new RoomTypeConfig(type, count, 20, 40);
            case AMPHI_COULOIR_BINETS:
                return new RoomTypeConfig(type, count, 50, 100);
            case SALLES_INFO:
                return new RoomTypeConfig(type, count, 20, 40);
            case SALLES_LANGUES:
                return new RoomTypeConfig(type, count, 15, 25);
            case NOUVEAUX_AMPHIS:
                return new RoomTypeConfig(type, count, 60, 120);
            case GRANDS_AMPHIS:
                return new RoomTypeConfig(type, count, 150, 400);
            case AMPHIS_80_100:
                return new RoomTypeConfig(type, count, 80, 100);
            default:
                return new RoomTypeConfig(type, count, 20, 50);
        }
    }

    /**
     * Generate room name based on type and counter
     */
    private String generateRoomName(RoomType type, int counter) {
        String prefix;
        switch (type) {
            case COULOIR_VANNEAU:
                prefix = "VAN";
                break;
            case COULOIR_SCOLARITE:
                prefix = "SCO";
                break;
            case COULOIR_LABOS:
                prefix = "LAB";
                break;
            case SALLES_100:
                prefix = "S1";
                break;
            case AMPHI_COULOIR_BINETS:
                prefix = "BIN";
                break;
            case SALLES_INFO:
                prefix = "INF";
                break;
            case SALLES_LANGUES:
                prefix = "LAN";
                break;
            case NOUVEAUX_AMPHIS:
                prefix = "NAM";
                break;
            case GRANDS_AMPHIS:
                prefix = "GAM";
                break;
            case AMPHIS_80_100:
                prefix = "AM";
                break;
            default:
                prefix = "RM";
        }
        return prefix + String.format("%02d", counter);
    }

    /**
     * Add random unavailable time slots to a room
     */
    private void addRandomUnavailableSlots(Room room, double unavailabilityRate) {
        DayOfWeek[] workDays = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};

        // Standard hours: 8AM to 8PM = 12 hours = 720 minutes
        // Possible slots: 60min, 90min, 120min, 180min sessions
        LocalTime dayStart = LocalTime.of(8, 0);

        for (DayOfWeek day : workDays) {
            // For each day, decide if we should block some time
            if (random.nextDouble() < unavailabilityRate) {
                // Block 1-3 random time slots
                int numBlocks = 1 + random.nextInt(3);

                for (int i = 0; i < numBlocks; i++) {
                    // Random duration: 60, 90, 120, or 180 minutes
                    int[] durations = {60, 90, 120, 180};
                    int duration = durations[random.nextInt(durations.length)];

                    // Random start time (must fit within the day)
                    int totalMinutes = 12 * 60; // 8AM to 8PM
                    int maxStartOffset = totalMinutes - duration;

                    if (maxStartOffset > 0) {
                        int startOffset = random.nextInt(maxStartOffset);
                        LocalTime start = dayStart.plusMinutes(startOffset);
                        LocalTime end = start.plusMinutes(duration);

                        room.addUnavailableSlot(day, start, end);
                    }
                }
            }
        }
    }

    /**
     * Add specific unavailable slots to multiple rooms (e.g., maintenance windows)
     */
    public void addMaintenanceWindows(List<Room> rooms, DayOfWeek day,
                                     LocalTime start, LocalTime end,
                                     double affectedRoomPercentage) {
        int numAffected = (int) (rooms.size() * affectedRoomPercentage);
        List<Room> shuffled = new ArrayList<>(rooms);
        Collections.shuffle(shuffled, random);

        for (int i = 0; i < numAffected && i < shuffled.size(); i++) {
            shuffled.get(i).addUnavailableSlot(day, start, end);
        }
    }
}
