package com.roomallocation.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Course {
    private final int[] possibleDurations = {60, 90, 120, 180, 200}; // in minutes

    private String name;
    private int cohortSize;
    private List<RoomType> typePreferences;
    private String assignedRoom;
    private int choicenumber; // Added choice number for the room allocation
    private int durationMinutes; // Duration of the course in minutes
    private String professorId; // Reference to the professor teaching this course

    public Course(String name, int cohortSize) {
        this.name = name;
        this.cohortSize = cohortSize;
        this.typePreferences = new ArrayList<>();
        this.assignedRoom = null;
        this.choicenumber = 0;
        this.durationMinutes = 60; // Default 60 minutes
        this.professorId = null;
    }

    public Course(String name, int cohortSize, int durationMinutes, String professorId) {
        this.name = name;
        this.cohortSize = cohortSize;
        this.typePreferences = new ArrayList<>();
        this.assignedRoom = null;
        this.choicenumber = 0;
        if (Arrays.stream(possibleDurations).anyMatch(d -> d == durationMinutes)) {
            this.durationMinutes = durationMinutes;
        } else {
            // default to closest lower duration if invalid
            System.out.println("Invalid duration specified for course " + name + ". Setting to closest 30 minutes.");
            this.durationMinutes = durationMinutes / 30 * 30;
        }
        this.professorId = professorId;
    }

    public void setTypePreferences(List<RoomType> preferences) {
        this.typePreferences = new ArrayList<>(preferences);
    }

    public String getName() { return name; }
    public int getCohortSize() { return cohortSize; }
    public List<RoomType> getTypePreferences() { return typePreferences; }
    public String getAssignedRoom() { return assignedRoom; }
    public void setAssignedRoom(String room) { this.assignedRoom = room; }
    public int getChoiceNumber() { return choicenumber; }
    public void incrementChoiceNumber() { this.choicenumber++; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) {
        if (Arrays.stream(possibleDurations).anyMatch(d -> d == durationMinutes)) {
            this.durationMinutes = durationMinutes;
        } else {
            System.out.println("Invalid duration specified for course " + name + ". Setting to closest 30 minutes.");
            this.durationMinutes = durationMinutes / 30 * 30;
        }
    }
    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }

    @Override
    public String toString() {
        return name + " by " + professorId + " (Size: " + cohortSize + ")";
    }

        @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(getName(), course.getName());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getName()) + Objects.hash(getCohortSize()) + Objects.hash(getProfessorId());
    }
}
