import java.util.*;

class Course {
    private String name;
    private int cohortSize;
    private List<String> preferences;
    private String assignedRoom;

    public Course(String name, int cohortSize) {
        this.name = name;
        this.cohortSize = cohortSize;
        this.preferences = new ArrayList<>();
        this.assignedRoom = null;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = new ArrayList<>(preferences);
    }

    public String getName() { return name; }
    public int getCohortSize() { return cohortSize; }
    public List<String> getPreferences() { return preferences; }
    public String getAssignedRoom() { return assignedRoom; }
    public void setAssignedRoom(String room) { this.assignedRoom = room; }

    @Override
    public String toString() {
        return name + " (Size: " + cohortSize + ")";
    }
}

class Room {
    private String name;
    private int capacity;
    private Course currentOccupant;

    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        this.currentOccupant = null;
    }

    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public Course getCurrentOccupant() { return currentOccupant; }
    public void setCurrentOccupant(Course course) { this.currentOccupant = course; }

    @Override
    public String toString() {
        return name + " (Capacity: " + capacity + ")";
    }
}

class AllocationStep {
    private String description;
    private Course course;
    private Room room;
    private Course displacedCourse;

    public AllocationStep(String description, Course course, Room room, Course displacedCourse) {
        this.description = description;
        this.course = course;
        this.room = room;
        this.displacedCourse = displacedCourse;
    }

    @Override
    public String toString() {
        return description;
    }
}

public class SizeBasedAllocation {
    private List<Course> courses;
    private List<Room> rooms;
    private LinkedList<Course> priorityQueue;
    private Map<String, String> assignments;
    private List<AllocationStep> steps;

    public SizeBasedAllocation(List<Course> courses, List<Room> rooms) {
        this.courses = new ArrayList<>(courses);
        this.rooms = new ArrayList<>(rooms);
        this.priorityQueue = new LinkedList<>();
        this.assignments = new HashMap<>();
        this.steps = new ArrayList<>();
    }

    private void initializePriorityQueue() {
        priorityQueue.clear();
        priorityQueue.addAll(courses);
    }

    private boolean isBetterFit(Course requestingCourse, Course currentOccupant, Room room) {
        if (currentOccupant == null) {
            return true;
        }

        boolean requestingFits = requestingCourse.getCohortSize() <= room.getCapacity();
        boolean currentFits = currentOccupant.getCohortSize() <= room.getCapacity();

        if (requestingFits && !currentFits) {
            return true;
        } else if (requestingFits && currentFits) {
            // Both fit, check efficiency
            double requestingEfficiency = (double) requestingCourse.getCohortSize() / room.getCapacity();
            double currentEfficiency = (double) currentOccupant.getCohortSize() / room.getCapacity();
            return requestingEfficiency > currentEfficiency;
        }

        return false;
    }

    public Map<String, String> allocate() {
        initializePriorityQueue();
        assignments.clear();
        steps.clear();

        while (!priorityQueue.isEmpty()) {
            Course currentCourse = priorityQueue.removeFirst();

            for (String preferredRoomName : currentCourse.getPreferences()) {
                Room room = rooms.stream()
                    .filter(r -> r.getName().equals(preferredRoomName))
                    .findFirst()
                    .orElse(null);

                if (room == null) continue;

                if (room.getCurrentOccupant() == null) {
                    if (currentCourse.getCohortSize() <= room.getCapacity()) {
                        room.setCurrentOccupant(currentCourse);
                        assignments.put(currentCourse.getName(), room.getName());
                        steps.add(new AllocationStep(
                            currentCourse.getName() + " assigned to " + room.getName(),
                            currentCourse, room, null
                        ));
                        break;
                    }
                } else {
                    if (isBetterFit(currentCourse, room.getCurrentOccupant(), room)) {
                        Course displacedCourse = room.getCurrentOccupant();
                        room.setCurrentOccupant(currentCourse);
                        assignments.put(currentCourse.getName(), room.getName());
                        steps.add(new AllocationStep(
                            currentCourse.getName() + " displaced " + displacedCourse.getName() + " from " + room.getName(),
                            currentCourse, room, displacedCourse
                        ));
                        
                        // Add displaced course back with high priority
                        priorityQueue.addFirst(displacedCourse);
                        break;
                    }
                }
            }
        }

        return assignments;
    }

    public List<AllocationStep> getSteps() {
        return steps;
    }

    public static void main(String[] args) {
        // Create rooms
        List<Room> rooms = Arrays.asList(
            new Room("Large Hall", 200),
            new Room("Medium Room", 100),
            new Room("Small Room", 50)
        );

        // Create courses with different sizes
        List<Course> courses = Arrays.asList(
            new Course("Math 101", 150),
            new Course("Physics 101", 80),
            new Course("Chemistry 101", 120),
            new Course("Biology 101", 90)
        );

        // Set preferences for each course
        List<String> defaultPreferences = Arrays.asList("Large Hall", "Medium Room", "Small Room");
        for (Course course : courses) {
            course.setPreferences(defaultPreferences);
        }

        // Run allocation
        SizeBasedAllocation allocator = new SizeBasedAllocation(courses, rooms);
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