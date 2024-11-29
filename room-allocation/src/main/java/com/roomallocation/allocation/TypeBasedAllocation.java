package com.roomallocation.allocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.roomallocation.fitmethods.capaFit;
import com.roomallocation.model.Room;
import com.roomallocation.model.RoomType;
import com.roomallocation.model.Course;

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
    private void clearAllAssignments() {
        // Clear all room assignments
        for (List<Room> rooms : roomsByType.values()) {
            for (Room room : rooms) {
                if (room.getCurrentOccupant() != null) {
                    room.getCurrentOccupant().setAssignedRoom(null);
                    room.setCurrentOccupant(null);
                }
            }
        }
        // Clear all course assignments
        for (Course course : courses) {
            course.setAssignedRoom(null);
        }
        assignments.clear();
    }

    public List<AllocationStep> getSteps() {
        return steps;
    }

    /**
     * Evaluates how well a course fits in a room based on size differences
     * Returns a score where lower is better (less wasted space)
     */

    /**
     * Find the best assignments for courses to rooms of a specific type,
     * maintaining the deferred acceptance principle
     */
    private Map<Room, Course> processTypeProposals(List<Course> proposingCourses, List<Room> rooms) {
        Map<Room, Course> currentMatches = new HashMap<>();
        Set<Course> availableCourses = new HashSet<>();  // Use Set instead of List to prevent duplicates
        
        // Add proposing courses to available pool
        availableCourses.addAll(proposingCourses);
        
        // First collect all currently assigned courses that might be displaced
        for (Room room : rooms) {
            Course currentOccupant = room.getCurrentOccupant();
            if (currentOccupant != null) {
                room.setCurrentOccupant(null);  // Clear the room
                availableCourses.add(currentOccupant);  // Set will prevent duplicates
            }
        }
        
        // Sort rooms by capacity
        rooms.sort(Comparator.comparingInt(Room::getCapacity));
        
        // For each room, find the best available course
        for (Room room : rooms) {
            Course bestCourse = null;
            double bestFitScore = Double.POSITIVE_INFINITY;
            
            // Evaluate all available courses for this room
            for (Course candidate : new ArrayList<>(availableCourses)) {
                double fitScore = capaFit.capafit(room, candidate);
                
                if (fitScore < bestFitScore) {
                    bestFitScore = fitScore;
                    bestCourse = candidate;
                }
            }
            
            if (bestCourse != null) {
                // Make the new assignment
                room.setCurrentOccupant(bestCourse);
                currentMatches.put(room, bestCourse);
                availableCourses.remove(bestCourse);  // Remove from available pool
                
                // Record the step
                steps.add(new AllocationStep(
                    bestCourse.getName() + " assigned to " + room.getName(),
                    bestCourse, room, null));
            }
        }
        
        return currentMatches;
    }

    public Map<String, String> allocate() {
    clearAllAssignments();
    steps.clear();
    List<Course> unassignableCourses = new ArrayList<>();
    Set<Course> unmatchedCourses = new HashSet<>(courses);
    Set<Course> globallyAssignedCourses = new HashSet<>();  // Track all assigned courses across iterations

    while (!unmatchedCourses.isEmpty()) {
        // Group proposals by room type
        Map<RoomType, List<Course>> proposalsByType = new HashMap<>();
        Iterator<Course> iterator = unmatchedCourses.iterator();
        Set<Course> assignedCourses = new HashSet<>();  // Track assigned courses in this iteration

        while (iterator.hasNext()) {
            Course course = iterator.next();
            int currentChoice = course.getChoiceNumber();

            if (globallyAssignedCourses.contains(course)) {  // Skip globally assigned courses
                iterator.remove();
                continue;
            }

            if (course.getAssignedRoom() != null) {  // If course is already assigned
                globallyAssignedCourses.add(course);
                iterator.remove();
                continue;
            }

            if (currentChoice >= course.getTypePreferences().size()) {
                unassignableCourses.add(course);
                iterator.remove();
                continue;
            }

            RoomType preferredType = course.getTypePreferences().get(currentChoice);
            proposalsByType.computeIfAbsent(preferredType, k -> new ArrayList<>()).add(course);
            course.incrementChoiceNumber();
        }

        for (Map.Entry<RoomType, List<Course>> entry : proposalsByType.entrySet()) {
            Map<Room, Course> typeMatches = processTypeProposals(entry.getValue(), 
                                                               roomsByType.get(entry.getKey()));
            typeMatches.forEach((room, course) -> {
                assignments.put(course.getName(), room.getName());
                assignedCourses.add(course);  // Mark course as assigned in this iteration
                globallyAssignedCourses.add(course);  // Mark course as globally assigned
            });
        }
        
        unmatchedCourses.removeAll(assignedCourses);
    }

    // Print allocation steps and unassignable courses
    // steps.forEach(System.out::println);
    // if (!unassignableCourses.isEmpty()) {
    //     System.out.println("\nUnassignable courses:");
    //     unassignableCourses.forEach(course -> {
    //         System.out.println(
    //                 course.getName() + " could not be assigned to any room (size: " + course.getCohortSize() + ")");
    //     });
    // }
    return assignments;
}

    public Map<String, Object> exportAllocationState() {
        Map<String, Object> state = new HashMap<>();

        // Export allocated rooms with their courses
        List<Map<String, Object>> roomsData = new ArrayList<>();
        for (RoomType type : roomsByType.keySet()) {
            for (Room room : roomsByType.get(type)) {
                Map<String, Object> roomData = new HashMap<>();
                roomData.put("name", room.getName());
                roomData.put("capacity", room.getCapacity());
                roomData.put("type", room.getType().name());

                Course occupant = room.getCurrentOccupant();
                if (occupant != null) {
                    Map<String, Object> courseData = new HashMap<>();
                    courseData.put("name", occupant.getName());
                    courseData.put("size", occupant.getCohortSize());
                    roomData.put("course", courseData);
                } else {
                    roomData.put("course", null);
                }

                roomsData.add(roomData);
            }
        }
        state.put("rooms", roomsData);

        // Export unallocated courses
        List<Map<String, Object>> unallocatedData = new ArrayList<>();
        for (Course course : courses) {
            if (course.getAssignedRoom() == null) {
                Map<String, Object> courseData = new HashMap<>();
                courseData.put("name", course.getName());
                courseData.put("size", course.getCohortSize());
                unallocatedData.add(courseData);
            }
        }
        state.put("unallocatedCourses", unallocatedData);

        return state;
    }
}
