import java.util.*;

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
    private Map<RoomType, List<Room>> roomsByType;
    private Map<String, String> assignments;
    private List<AllocationStep> steps;

    public TypeBasedAllocation(List<Course> courses, List<Room> rooms) {
        this.courses = new ArrayList<>(courses);
        this.roomsByType = new EnumMap<>(RoomType.class);
        this.assignments = new HashMap<>();
        this.steps = new ArrayList<>();

        // Initialize roomsByType map
        for (RoomType type : RoomType.values()) {
            roomsByType.put(type, new ArrayList<>());
        }

        // Organize rooms by type
        for (Room room : rooms) {
            roomsByType.get(room.getType()).add(room);
            // Sort rooms by capacity within each type
            roomsByType.get(room.getType()).sort(Comparator.comparingInt(Room::getCapacity));
        }
    }

    /**
     * Evaluates how well a course fits in a room based on size differences
     * Returns a score where lower is better (less wasted space)
     */
    private int evaluateFit(Course course, Room room) {
        if (course.getCohortSize() > room.getCapacity()) {
            return Integer.MAX_VALUE; // Room is too small
        }
        return room.getCapacity() - course.getCohortSize(); // Minimize wasted space
    }

    /**
     * Find the best assignments for courses to rooms of a specific type,
     * maintaining the deferred acceptance principle
     */
    private Map<Room, Course> processTypeProposals(List<Course> proposingCourses, List<Room> rooms) {
        Map<Room, Course> currentMatches = new HashMap<>();
        
        // Get current occupants
        for (Room room : rooms) {
            Course currentOccupant = room.getCurrentOccupant();
            if (currentOccupant != null) {
                currentMatches.put(room, currentOccupant);
            }
        }

        // Consider all proposals including current occupants
        List<Course> allCandidates = new ArrayList<>(proposingCourses);
        currentMatches.values().forEach(course -> {
            if (!allCandidates.contains(course)) {
                allCandidates.add(course);
            }
        });

        // For each room, evaluate all candidates and keep the best fit
        for (Room room : rooms) {
            Course bestCandidate = null;
            int bestFitScore = Integer.MAX_VALUE;
            
            for (Course candidate : allCandidates) {
                int fitScore = evaluateFit(candidate, room);
                if (fitScore < bestFitScore) {
                    bestFitScore = fitScore;
                    bestCandidate = candidate;
                }
            }

            if (bestCandidate != null && bestFitScore != Integer.MAX_VALUE) {
                Course currentOccupant = currentMatches.get(room);
                if (!bestCandidate.equals(currentOccupant)) {
                    if (currentOccupant != null) {
                        steps.add(new AllocationStep(
                            String.format("%s displaced %s from %s (%s)",
                                bestCandidate.getName(),
                                currentOccupant.getName(),
                                room.getName(),
                                room.getType().getDisplayName()),
                            bestCandidate, room, currentOccupant));
                    } else {
                        steps.add(new AllocationStep(
                            String.format("%s assigned to %s (%s)",
                                bestCandidate.getName(),
                                room.getName(),
                                room.getType().getDisplayName()),
                            bestCandidate, room, null));
                    }
                    currentMatches.put(room, bestCandidate);
                }
            }
        }

        return currentMatches;
    }

    public Map<String, String> allocate() {
        assignments.clear();
        steps.clear();
        List<Course> unassignableCourses = new ArrayList<>();
        Set<Course> unmatchedCourses = new HashSet<>(courses);

        while (!unmatchedCourses.isEmpty()) {
            // Group proposals by room type
            Map<RoomType, List<Course>> proposalsByType = new HashMap<>();
            Iterator<Course> iterator = unmatchedCourses.iterator();
            
            while (iterator.hasNext()) {
                Course course = iterator.next();
                int currentChoice = course.getChoiceNumber();
                
                if (currentChoice >= course.getTypePreferences().size()) {
                    unassignableCourses.add(course);
                    iterator.remove();
                    continue;
                }

                RoomType preferredType = course.getTypePreferences().get(currentChoice);
                proposalsByType.computeIfAbsent(preferredType, k -> new ArrayList<>()).add(course);
                course.incrementChoiceNumber();
            }

            // Process each room type's proposals using deferred acceptance
            Set<Course> newlyMatched = new HashSet<>();
            
            for (Map.Entry<RoomType, List<Course>> entry : proposalsByType.entrySet()) {
                RoomType type = entry.getKey();
                List<Course> proposals = entry.getValue();
                List<Room> availableRooms = roomsByType.get(type);

                Map<Room, Course> typeMatches = processTypeProposals(proposals, availableRooms);
                
                // Update room assignments and track matched courses
                typeMatches.forEach((room, course) -> {
                    room.setCurrentOccupant(course);
                    assignments.put(course.getName(), room.getName());
                    newlyMatched.add(course);
                });
            }

            // Remove successfully matched courses from unmatched set
            unmatchedCourses.removeAll(newlyMatched);
            
            // If no new matches were made and we still have unmatched courses,
            // they will try their next preferences in the next iteration
        }

        // Print allocation steps and unassignable courses
        steps.forEach(System.out::println);
        if (!unassignableCourses.isEmpty()) {
            System.out.println("\nUnassignable courses:");
            unassignableCourses.forEach(course -> 
                System.out.println(course.getName() + " could not be assigned to any room"));
        }

        return assignments;
    }
}