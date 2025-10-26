package com.roomallocation.constraint;

import com.roomallocation.model.Professor;
import com.roomallocation.model.Schedule;
import com.roomallocation.model.ScheduledCourse;
import com.roomallocation.model.SessionPattern;
import com.roomallocation.model.TimeSlot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates professor availability and prevents double-booking
 */
public class ProfessorConstraint {

    /**
     * Check for professor constraint violations
     * Returns list of violation messages
     */
    public List<String> validateConstraints(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        // Check availability violations
        violations.addAll(checkAvailabilityViolations(schedule));

        // Check double-booking violations
        violations.addAll(checkDoubleBookingViolations(schedule));

        return violations;
    }

    /**
     * Check if professors are available for their assigned courses
     */
    private List<String> checkAvailabilityViolations(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        for (ScheduledCourse scheduledCourse : schedule.getScheduledCourses()) {
            if (!scheduledCourse.isScheduled()) {
                continue;
            }

            Professor professor = schedule.getProfessorForCourse(scheduledCourse.getCourse());
            if (professor == null) {
                continue; // No professor assigned
            }

            if (!scheduledCourse.isProfessorAvailable(professor)) {
                violations.add(String.format(
                    "AVAILABILITY VIOLATION: Professor %s is not available for %s at %s",
                    professor.getName(),
                    scheduledCourse.getCourse().getName(),
                    scheduledCourse.getSessionPattern()
                ));
            }
        }

        return violations;
    }

    /**
     * Check if any professor is double-booked
     */
    private List<String> checkDoubleBookingViolations(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        // Group courses by professor
        Map<String, List<ScheduledCourse>> coursesByProfessor = new HashMap<>();

        for (ScheduledCourse sc : schedule.getScheduledCourses()) {
            if (!sc.isScheduled()) {
                continue;
            }

            String professorId = sc.getCourse().getProfessorId();
            if (professorId != null) {
                coursesByProfessor.computeIfAbsent(professorId, k -> new ArrayList<>()).add(sc);
            }
        }

        // Check for overlaps within each professor's courses
        for (Map.Entry<String, List<ScheduledCourse>> entry : coursesByProfessor.entrySet()) {
            String professorId = entry.getKey();
            List<ScheduledCourse> courses = entry.getValue();
            Professor professor = schedule.getProfessors().get(professorId);

            for (int i = 0; i < courses.size(); i++) {
                for (int j = i + 1; j < courses.size(); j++) {
                    ScheduledCourse course1 = courses.get(i);
                    ScheduledCourse course2 = courses.get(j);

                    if (course1.hasTimeConflictWith(course2)) {
                        violations.add(String.format(
                            "DOUBLE-BOOKING VIOLATION: Professor %s is assigned to both %s and %s at overlapping times",
                            professor != null ? professor.getName() : professorId,
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
     * Calculate penalty for professor schedule quality
     * Penalizes large gaps between classes
     */
    public double calculateScheduleQualityPenalty(Schedule schedule) {
        double penalty = 0.0;

        // Group courses by professor
        Map<String, List<ScheduledCourse>> coursesByProfessor = new HashMap<>();

        for (ScheduledCourse sc : schedule.getScheduledCourses()) {
            if (!sc.isScheduled()) {
                continue;
            }

            String professorId = sc.getCourse().getProfessorId();
            if (professorId != null) {
                coursesByProfessor.computeIfAbsent(professorId, k -> new ArrayList<>()).add(sc);
            }
        }

        // Calculate gap penalty for each professor
        for (List<ScheduledCourse> courses : coursesByProfessor.values()) {
            penalty += calculateProfessorGapPenalty(courses);
        }

        return penalty;
    }

    /**
     * Calculate penalty based on gaps in professor's schedule
     * Prefer compact schedules (classes close together)
     */
    private double calculateProfessorGapPenalty(List<ScheduledCourse> courses) {
        if (courses.size() <= 1) {
            return 0.0;
        }

        double penalty = 0.0;

        // Collect all time slots
        List<TimeSlot> allSlots = new ArrayList<>();
        for (ScheduledCourse course : courses) {
            allSlots.addAll(course.getSessionPattern().getSessions());
        }

        // Group by day and calculate gaps
        Map<String, List<TimeSlot>> slotsByDay = new HashMap<>();
        for (TimeSlot slot : allSlots) {
            slotsByDay.computeIfAbsent(slot.getDay().toString(), k -> new ArrayList<>()).add(slot);
        }

        // For each day, calculate gap penalty
        for (List<TimeSlot> daySlots : slotsByDay.values()) {
            if (daySlots.size() <= 1) {
                continue;
            }

            // Sort by start time
            daySlots.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));

            // Calculate gaps between consecutive classes
            for (int i = 0; i < daySlots.size() - 1; i++) {
                LocalTime endTime = daySlots.get(i).getEndTime();
                LocalTime nextStartTime = daySlots.get(i + 1).getStartTime();

                int gapMinutes = (nextStartTime.toSecondOfDay() - endTime.toSecondOfDay()) / 60;

                // Penalty for gaps > 60 minutes
                if (gapMinutes > 60) {
                    penalty += (gapMinutes - 60) * 0.1; // Small penalty per extra minute
                }
            }
        }

        return penalty;
    }

    /**
     * Check if assigning a session pattern would violate professor constraints
     */
    public boolean wouldViolateConstraints(ScheduledCourse course, SessionPattern pattern, Schedule schedule) {
        Professor professor = schedule.getProfessorForCourse(course.getCourse());
        if (professor == null) {
            return false; // No professor assigned
        }

        // Check availability
        if (!pattern.fitsAvailability(professor)) {
            return true;
        }

        // Check for double-booking with other courses
        List<ScheduledCourse> professorCourses = schedule.getCoursesByProfessor(professor.getId());
        for (ScheduledCourse otherCourse : professorCourses) {
            if (otherCourse.equals(course) || !otherCourse.isScheduled()) {
                continue;
            }

            if (pattern.hasOverlapWith(otherCourse.getSessionPattern())) {
                return true;
            }
        }

        return false;
    }
}
