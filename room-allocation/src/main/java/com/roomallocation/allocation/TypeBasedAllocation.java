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
        List<Course> allCandidates = new ArrayList<>(proposingCourses);

        // Get current occupants
        for (Room room : rooms) {
            Course currentOccupant = room.getCurrentOccupant();
            if (currentOccupant != null) {
                allCandidates.add(currentOccupant);
            }
        }

        // Sort rooms by increasing capacity for better space utilization
        rooms.sort(Comparator.comparingInt(Room::getCapacity));

        // For each room, evaluate all candidates and keep the best fit
        for (Room room : rooms) {
            Course bestCandidate = null;
            double bestFitScore = Double.POSITIVE_INFINITY;

            // Evaluate all candidates for this room
            for (Course candidate : allCandidates) {
                double fitScore = capaFit.capafit(room, candidate);
                if (fitScore < bestFitScore) {
                    bestFitScore = fitScore;
                    bestCandidate = candidate;
                }
            }

            if (bestCandidate != null) {
                Course previousOccupant = room.getCurrentOccupant();

                // Update assignments
                if (previousOccupant == null) {
                    currentMatches.put(room, bestCandidate);
                    steps.add(new AllocationStep(
                            bestCandidate.getName() + " assigned to " + room.getName(),
                            bestCandidate, room, null));
                } else if (bestCandidate != previousOccupant) {
                    currentMatches.put(room, bestCandidate);
                    steps.add(new AllocationStep(
                            bestCandidate.getName() + " displaces " + previousOccupant.getName() +
                                    " in " + room.getName(),
                            bestCandidate, room, previousOccupant));
                }

                // Remove the assigned candidate from future consideration
                allCandidates.remove(bestCandidate);
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
            unassignableCourses.forEach(course -> {
                System.out.println(
                        course.getName() + " could not be assigned to any room (size: " + course.getCohortSize() + ")");
                // System.out.println("Preferences: " + course.getTypePreferences());
            });
        }
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
