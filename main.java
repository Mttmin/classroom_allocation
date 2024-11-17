import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Create rooms with types
        List<Room> rooms = RoomDataLoader.loadRooms();
        PreferenceGenerationStrategy sizestrategy = new SizedBasedPreferenceStrategy(10, rooms);
        PreferenceGenerationStrategy smartRandom = new SmartRandomPreferenceStrategy(10, rooms);
        // Create courses with different sizes
        Coursesimulator simulator = new Coursesimulator(new RandomPreferenceStrategy(10));
        List<Course> courses = simulator.generateCourses(70, 10, 200, 40);

        for (Course course : courses) {
            System.out.println(course.getName() + " preferences: " + course.getTypePreferences());
        }
        // Run allocation
        TypeBasedAllocation allocator = new TypeBasedAllocation(courses, rooms);
        Map<String, String> finalAssignments = allocator.allocate();

        System.out.println("\nFinal Assignments:");
        courses.stream()
            .filter(course -> course.getAssignedRoom() != null)
            .forEach(course -> System.out.printf("%s -> %s (Choice #%d)%n", 
                course.getName(), 
                course.getAssignedRoom(), 
                course.getChoiceNumber()));
    }
}
