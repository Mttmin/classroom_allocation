import java.util.*;

class Course {
    private String name;
    private int cohortSize;
    private List<RoomType> typePreferences;
    private String assignedRoom;

    public Course(String name, int cohortSize) {
        this.name = name;
        this.cohortSize = cohortSize;
        this.typePreferences = new ArrayList<>();
        this.assignedRoom = null;
    }

    public void setTypePreferences(List<RoomType> preferences) {
        this.typePreferences = new ArrayList<>(preferences);
    }

    public String getName() { return name; }
    public int getCohortSize() { return cohortSize; }
    public List<RoomType> getTypePreferences() { return typePreferences; }
    public String getAssignedRoom() { return assignedRoom; }
    public void setAssignedRoom(String room) { this.assignedRoom = room; }

    @Override
    public String toString() {
        return name + " (Size: " + cohortSize + ")";
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

public class TypeBasedAllocation {
    private List<Course> courses;
    private Map<RoomType, List<Room>> roomsByType;  // Organize rooms by type
    private LinkedList<Course> priorityQueue;
    private Map<String, String> assignments;
    private List<AllocationStep> steps;

    public TypeBasedAllocation(List<Course> courses, List<Room> rooms) {
        this.courses = new ArrayList<>(courses);
        this.roomsByType = new EnumMap<>(RoomType.class);
        this.priorityQueue = new LinkedList<>();
        this.assignments = new HashMap<>();
        this.steps = new ArrayList<>();
        
        // Initialize roomsByType map
        for (RoomType type : RoomType.values()) {
            roomsByType.put(type, new ArrayList<>());
        }
        
        // Organize rooms by type
        for (Room room : rooms) {
            roomsByType.get(room.getType()).add(room);
        }
    }

    private void initializePriorityQueue() {
        priorityQueue.clear();
        // Sort courses by size (larger courses get higher priority)
        courses.sort((c1, c2) -> Integer.compare(c2.getCohortSize(), c1.getCohortSize()));
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

            // Try each preferred room type in order
            for (RoomType preferredType : currentCourse.getTypePreferences()) {
                List<Room> availableRooms = roomsByType.get(preferredType);
                
                // Sort rooms by capacity (closest to course size gets priority)
                availableRooms.sort((r1, r2) -> {
                    int diff1 = Math.abs(r1.getCapacity() - currentCourse.getCohortSize());
                    int diff2 = Math.abs(r2.getCapacity() - currentCourse.getCohortSize());
                    return Integer.compare(diff1, diff2);
                });

                boolean assigned = false;
                for (Room room : availableRooms) {
                    if (room.getCurrentOccupant() == null) {
                        if (currentCourse.getCohortSize() <= room.getCapacity()) {
                            room.setCurrentOccupant(currentCourse);
                            assignments.put(currentCourse.getName(), room.getName());
                            steps.add(new AllocationStep(
                                String.format("%s assigned to %s (%s)", 
                                    currentCourse.getName(), 
                                    room.getName(),
                                    room.getType().getDisplayName()),
                                currentCourse, room, null
                            ));
                            assigned = true;
                            break;
                        }
                    } else if (isBetterFit(currentCourse, room.getCurrentOccupant(), room)) {
                        Course displacedCourse = room.getCurrentOccupant();
                        room.setCurrentOccupant(currentCourse);
                        assignments.put(currentCourse.getName(), room.getName());
                        steps.add(new AllocationStep(
                            String.format("%s displaced %s from %s (%s)", 
                                currentCourse.getName(),
                                displacedCourse.getName(),
                                room.getName(),
                                room.getType().getDisplayName()),
                            currentCourse, room, displacedCourse
                        ));
                        
                        priorityQueue.addFirst(displacedCourse);
                        assigned = true;
                        break;
                    }
                }
                
                if (assigned) break;
            }
        }

        return assignments;
    }

    public List<AllocationStep> getSteps() {
        return steps;
    }
    public static void main(String[] args) {
        // Create rooms with types
        List<Room> rooms = RoomDataLoader.loadRooms();
        PreferenceGenerationStrategy sizestrategy = new SizedBasedPreferenceStrategy(5, rooms);
        PreferenceGenerationStrategy smartRandom = new SmartRandomPreferenceStrategy(5, rooms);
        // Create courses with different sizes
        List<Course> courses = Arrays.asList(
            new Course("Math Course", 50),
            new Course("Physics Lab", 45),
            new Course("Computer Science", 25)
        );

        // Set type preferences for each course
        courses.get(0).setTypePreferences(smartRandom.generatePreferences(courses.get(0)));
        courses.get(1).setTypePreferences(smartRandom.generatePreferences(courses.get(1)));
        courses.get(2).setTypePreferences(smartRandom.generatePreferences(courses.get(2)));

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
