import java.util.*;

abstract class PreferenceGenerationStrategy {
    protected final List<RoomType> availableRoomTypes;
    protected final Random random;

    public PreferenceGenerationStrategy() {
        this.availableRoomTypes = Arrays.asList(RoomType.values());
        this.random = new Random();
    }

    /**
     * Generate room type preferences for a given course
     * @param course The course to generate preferences for
     * @return List of room type preferences
     */
    public abstract List<RoomType> generatePreferences(Course course);

    /**
     * Helper method to shuffle a copy of the available room types
     * @return Shuffled list of room types
     */
    protected List<RoomType> getShuffledRoomTypes() {
        List<RoomType> shuffled = new ArrayList<>(availableRoomTypes);
        Collections.shuffle(shuffled, random);
        return shuffled;
    }
}

class RandomPreferenceStrategy extends PreferenceGenerationStrategy {
    private final int numPreferences;

    public RandomPreferenceStrategy(int numPreferences) {
        super();
        this.numPreferences = numPreferences;
    }

    @Override
    public List<RoomType> generatePreferences(Course course) {
        List<RoomType> preferences = getShuffledRoomTypes();
        return preferences.subList(0, Math.min(numPreferences, preferences.size()));
    }
}

class SizedBasedPreferenceStrategy extends PreferenceGenerationStrategy{
    private final int numPreferences;
    private int extraCapacity = 10; // Extra capacity to account for growth
    private final Map<RoomType, Integer> medianCapacities;

    public int getExtraCapacity() {
        return extraCapacity;
    }

    public void setExtraCapacity(int extraCapacity) {
        this.extraCapacity = extraCapacity;
    }

    public SizedBasedPreferenceStrategy(int numPreferences, List<Room> rooms) {
        super();
        this.numPreferences = numPreferences;
        this.medianCapacities = calculateMedianCapacities(rooms);
    }

    private Map<RoomType, Integer> calculateMedianCapacities(List<Room> rooms) {
        Map<RoomType, List<Integer>> capacities = new EnumMap<>(RoomType.class);
        for (RoomType type : RoomType.values()) {
            capacities.put(type, new ArrayList<>());
        }

        // Collect all capacities by room type
        for (Room room : rooms) {
            capacities.get(room.getType()).add(room.getCapacity());
        }

        // Calculate medians
        Map<RoomType, Integer> medians = new EnumMap<>(RoomType.class);
        for (Map.Entry<RoomType, List<Integer>> entry : capacities.entrySet()) {
            List<Integer> roomCapacities = entry.getValue();
            if (!roomCapacities.isEmpty()) {
                // Sort the capacities
                List<Integer> sorted = new ArrayList<>(roomCapacities);
                Collections.sort(sorted);
                
                // Calculate median
                int size = sorted.size();
                int median;
                if (size % 2 == 0) {
                    // If even number of elements, average the two middle values
                    median = (sorted.get((size - 1) / 2) + sorted.get(size / 2)) / 2;
                } else {
                    // If odd number of elements, take the middle value
                    median = sorted.get(size / 2);
                }
                medians.put(entry.getKey(), median);
            } else {
                medians.put(entry.getKey(), 0);
            }
        }

        return medians;
    }

    public List<RoomType> generatePreferences(Course course) {
        // Sort room types by how well they match the course size
        List<RoomType> sortedTypes = new ArrayList<>(availableRoomTypes);
        int courseSize = course.getCohortSize()+extraCapacity;
        
        sortedTypes.sort((type1, type2) -> {
            int diff1 = Math.abs(medianCapacities.get(type1) - courseSize);
            int diff2 = Math.abs(medianCapacities.get(type2) - courseSize);
            return Integer.compare(diff1, diff2);
        });

        // Return top 3 best fitting room types
        return sortedTypes.subList(0, Math.min(numPreferences, sortedTypes.size()));
    }
}

class SmartRandomPreferenceStrategy extends PreferenceGenerationStrategy {
    private final int maxPreferences;
    private final Map<RoomType, Integer> maxCapacities;

    public SmartRandomPreferenceStrategy(int maxPreferences, List<Room> rooms) {
        super();
        this.maxPreferences = maxPreferences;
        this.maxCapacities = new EnumMap<>(RoomType.class);
        
        // Find maximum capacity for each room type
        for (Room room : rooms) {
            maxCapacities.merge(room.getType(), room.getCapacity(), Math::max);
        }
    }

    @Override
    public List<RoomType> generatePreferences(Course course) {
        // Filter room types that could potentially fit the course
        List<RoomType> suitableTypes = new ArrayList<>();
        for (RoomType type : availableRoomTypes) {
            if (course.getCohortSize() <= maxCapacities.get(type)) {
                suitableTypes.add(type);
            }
        }
        // Randomly select from suitable types
        List<RoomType> shuffled = new ArrayList<>(suitableTypes);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, Math.min(maxPreferences, shuffled.size()));
    }
}

