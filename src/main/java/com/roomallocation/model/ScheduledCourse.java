package com.roomallocation.model;

import java.util.Objects;

/**
 * Represents a course with its assigned schedule
 */
public class ScheduledCourse {
    private Course course;
    private SessionPattern sessionPattern;
    private String assignedRoomId;
    private boolean isScheduled;

    public ScheduledCourse(Course course) {
        this.course = course;
        this.sessionPattern = new SessionPattern();
        this.assignedRoomId = null;
        this.isScheduled = false;
    }

    public ScheduledCourse(Course course, SessionPattern sessionPattern) {
        this.course = course;
        this.sessionPattern = sessionPattern;
        this.assignedRoomId = null;
        this.isScheduled = !sessionPattern.isEmpty();
    }

    /**
     * Assign a session pattern to this course
     */
    public void assignSessionPattern(SessionPattern pattern) {
        this.sessionPattern = pattern;
        this.isScheduled = !pattern.isEmpty();
    }

    /**
     * Assign a room to this scheduled course
     */
    public void assignRoom(String roomId) {
        this.assignedRoomId = roomId;
        this.course.setAssignedRoom(roomId);
    }

    /**
     * Clear the schedule for this course
     */
    public void clearSchedule() {
        this.sessionPattern = new SessionPattern();
        this.isScheduled = false;
    }

    /**
     * Check if this course conflicts with another (time overlap with student correlation)
     */
    public boolean hasTimeConflictWith(ScheduledCourse other) {
        if (!this.isScheduled || !other.isScheduled) {
            return false;
        }
        return this.sessionPattern.hasOverlapWith(other.sessionPattern);
    }

    /**
     * Check if professor is available for this schedule
     */
    public boolean isProfessorAvailable(Professor professor) {
        if (!isScheduled) {
            return true; // No schedule yet, so no conflict
        }
        return sessionPattern.fitsAvailability(professor);
    }

    public Course getCourse() {
        return course;
    }

    public SessionPattern getSessionPattern() {
        return sessionPattern;
    }

    public String getAssignedRoomId() {
        return assignedRoomId;
    }

    public boolean isScheduled() {
        return isScheduled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduledCourse that = (ScheduledCourse) o;
        return Objects.equals(course, that.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course);
    }

    @Override
    public String toString() {
        String scheduleInfo = isScheduled ? sessionPattern.toString() : "Not scheduled";
        String roomInfo = assignedRoomId != null ? ", Room: " + assignedRoomId : "";
        return course.getName() + " - " + scheduleInfo + roomInfo;
    }
}
