package com.roomallocation.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

public class Room {
    private String name;
    private int capacity;
    private RoomType type;
    private Course currentOccupant;
    private Map<DayOfWeek, List<TimeSlot>> unavailableSlots;

    public Room(String name, int capacity, RoomType type) {
        this.name = name;
        this.capacity = capacity;
        this.type = type;
        this.currentOccupant = null;
        this.unavailableSlots = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public RoomType getType() {
        return type;
    }

    public Course getCurrentOccupant() {
        return currentOccupant;
    }

    public void setCurrentOccupant(Course course) {
        this.currentOccupant = course;
        if (course != null) {
            course.setAssignedRoom(name);
        }
    }
    
    /**
     * Mark a time slot as unavailable for this room
     */
    public void addUnavailableSlot(TimeSlot timeSlot) {
        List<TimeSlot> slots = unavailableSlots.computeIfAbsent(timeSlot.getDay(), k -> new ArrayList<>());
        
        // Check if this slot overlaps with existing ones
        boolean overlaps = slots.stream().anyMatch(slot -> slot.overlapsWith(timeSlot));
        if (!overlaps) {
            slots.add(timeSlot);
            slots.sort(Comparator.comparing(TimeSlot::getStartTime));
        }
    }
    
    /**
     * Mark a time slot as unavailable using day and times
     */
    public void addUnavailableSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        TimeSlot timeSlot = new TimeSlot(day, startTime, endTime);
        addUnavailableSlot(timeSlot);
    }
    
    /**
     * Remove an unavailable time slot (make it available again)
     */
    public void removeUnavailableSlot(TimeSlot timeSlot) {
        List<TimeSlot> slots = unavailableSlots.get(timeSlot.getDay());
        if (slots != null) {
            slots.removeIf(slot -> slot.equals(timeSlot));
        }
    }
    
    /**
     * Remove an unavailable time slot using day and times
     */
    public void removeUnavailableSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        TimeSlot timeSlot = new TimeSlot(day, startTime, endTime);
        removeUnavailableSlot(timeSlot);
    }
    
    /**
     * Check if the room is available during a specific time slot
     */
    public boolean isAvailable(TimeSlot timeSlot) {
        List<TimeSlot> slots = unavailableSlots.get(timeSlot.getDay());
        if (slots == null || slots.isEmpty()) {
            return true; // No unavailable slots means it's available
        }
        
        // Check if the requested time slot overlaps with any unavailable slot
        return slots.stream().noneMatch(slot -> slot.overlapsWith(timeSlot));
    }
    
    /**
     * Check if the room is available using day and times
     */
    public boolean isAvailable(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        TimeSlot timeSlot = new TimeSlot(day, startTime, endTime);
        return isAvailable(timeSlot);
    }
    
    /**
     * Get all unavailable slots for a specific day
     */
    public List<TimeSlot> getUnavailableSlotsForDay(DayOfWeek day) {
        List<TimeSlot> slots = unavailableSlots.get(day);
        return slots != null ? new ArrayList<>(slots) : new ArrayList<>();
    }
    
    /**
     * Get all unavailable slots
     */
    public Map<DayOfWeek, List<TimeSlot>> getAllUnavailableSlots() {
        Map<DayOfWeek, List<TimeSlot>> copy = new HashMap<>();
        for (Map.Entry<DayOfWeek, List<TimeSlot>> entry : unavailableSlots.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
    
    /**
     * Clear all unavailable slots
     */
    public void clearUnavailableSlots() {
        unavailableSlots.clear();
    }

    @Override
    public String toString() {
        return name + " (Capacity: " + capacity + ", Type: " + type.getDisplayName() + ")";

    }
}
