package com.roomallocation.constraint;

import com.roomallocation.model.Schedule;
import com.roomallocation.model.ScheduledCourse;
import com.roomallocation.model.SessionPattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates that rooms are not double-booked, even if this is already ensured by the classroom allocation algorithm.
 */
public class RoomConstraint {

    /**
     * Check for room double-booking violations
     * Returns list of violation messages
     */
    public List<String> validateConstraints(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        // Group courses by room
        Map<String, List<ScheduledCourse>> coursesByRoom = new HashMap<>();

        for (ScheduledCourse sc : schedule.getScheduledCourses()) {
            if (!sc.isScheduled() || sc.getAssignedRoomId() == null) {
                continue;
            }

            coursesByRoom.computeIfAbsent(sc.getAssignedRoomId(), k -> new ArrayList<>()).add(sc);
        }

        // Check for overlaps within each room
        for (Map.Entry<String, List<ScheduledCourse>> entry : coursesByRoom.entrySet()) {
            String roomId = entry.getKey();
            List<ScheduledCourse> courses = entry.getValue();

            for (int i = 0; i < courses.size(); i++) {
                for (int j = i + 1; j < courses.size(); j++) {
                    ScheduledCourse course1 = courses.get(i);
                    ScheduledCourse course2 = courses.get(j);

                    if (course1.hasTimeConflictWith(course2)) {
                        violations.add(String.format(
                            "ROOM DOUBLE-BOOKING: Room %s is assigned to both %s and %s at overlapping times",
                            roomId,
                            course1.getCourse().getName(),
                            course2.getCourse().getName()
                        ));
                    }
                }
            }
        }

        return violations;
    }

    /**
     * Check if assigning a room would cause double-booking
     */
    public boolean wouldCauseDoubleBooking(ScheduledCourse newCourse, String roomId, Schedule schedule) {
        if (!newCourse.isScheduled()) {
            return false; // No schedule, can't double-book
        }

        SessionPattern newPattern = newCourse.getSessionPattern();

        // Check all other courses assigned to this room
        for (ScheduledCourse sc : schedule.getScheduledCourses()) {
            if (sc.equals(newCourse) || !sc.isScheduled()) {
                continue;
            }

            if (roomId.equals(sc.getAssignedRoomId())) {
                if (newPattern.hasOverlapWith(sc.getSessionPattern())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get available rooms for a given session pattern
     * Returns list of room IDs that don't have conflicts
     */
    public List<String> getAvailableRooms(SessionPattern pattern, List<String> allRoomIds, Schedule schedule) {
        List<String> availableRooms = new ArrayList<>();

        for (String roomId : allRoomIds) {
            boolean isAvailable = true;

            // Check if this room has any conflicts with the pattern
            for (ScheduledCourse sc : schedule.getScheduledCourses()) {
                if (!sc.isScheduled() || !roomId.equals(sc.getAssignedRoomId())) {
                    continue;
                }

                if (pattern.hasOverlapWith(sc.getSessionPattern())) {
                    isAvailable = false;
                    break;
                }
            }

            if (isAvailable) {
                availableRooms.add(roomId);
            }
        }

        return availableRooms;
    }
}
