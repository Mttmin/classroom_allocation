import java.util.List;
import java.util.Map;

public class main {
    public static void main(String[] args) {
        // Create rooms with types
        List<Room> rooms = RoomDataLoader.loadRooms();
        PreferenceGenerationStrategy sizestrategy = new SizedBasedPreferenceStrategy(5, rooms);
        PreferenceGenerationStrategy smartRandom = new SmartRandomPreferenceStrategy(5, rooms);
        // Create courses with different sizes
        Coursesimulator simulator = new Coursesimulator(new RandomPreferenceStrategy(5));
        List<Course> courses = simulator.generateCourses(80, 10, 200, 40);

        for (Course course : courses) {
            System.out.println(course.getName() + " preferences: " + course.getTypePreferences());
        }
        // Run allocation
        TypeBasedAllocation allocator = new TypeBasedAllocation(courses, rooms);
        Map<String, String> finalAssignments = allocator.allocate();

        // Print results
        System.out.println("\nAllocation Steps:");
        for (AllocationStep step : allocator.getSteps()) {
            System.out.println("- " + step);
        }

        System.out.println("\nFinal Assignments:");
        finalAssignments.forEach((course, room) -> 
            System.out.println(course + " -> " + room));
    }
}
