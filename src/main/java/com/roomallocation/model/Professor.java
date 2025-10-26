package com.roomallocation.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a professor with their availability schedule
 */
public class Professor {
    private String id;
    private String name;
    private Map<DayOfWeek, List<AvailabilityPeriod>> availability;

    public Professor(String id, String name) {
        this.id = id;
        this.name = name;
        this.availability = new HashMap<>();
        // Default: available all weekdays 8am-8pm
        initializeDefaultAvailability();
    }

    private void initializeDefaultAvailability() {
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                List<AvailabilityPeriod> periods = new ArrayList<>();
                periods.add(new AvailabilityPeriod(
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0)
                ));
                availability.put(day, periods);
            }
        }
    }

    /**
     * Check if professor is available during a specific time slot
     */
    public boolean isAvailable(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        List<AvailabilityPeriod> periods = availability.get(day);
        if (periods == null || periods.isEmpty()) {
            return false;
        }
        
        // Check if the requested time slot is fully covered by any availability period
        for (AvailabilityPeriod period : periods) {
            if (period.isAvailable(startTime, endTime)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if professor is available during a specific TimeSlot
     */
    public boolean isAvailable(TimeSlot timeSlot) {
        return isAvailable(timeSlot.getDay(), timeSlot.getStartTime(), timeSlot.getEndTime());
    }

    /**
     * Set availability for a specific day (replaces all existing periods for that day)
     */
    public void setAvailability(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        List<AvailabilityPeriod> periods = new ArrayList<>();
        periods.add(new AvailabilityPeriod(startTime, endTime));
        availability.put(day, periods);
    }
    
    /**
     * Add an availability period for a specific day (keeps existing periods)
     */
    public void addAvailabilityPeriod(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        List<AvailabilityPeriod> periods = availability.computeIfAbsent(day, k -> new ArrayList<>());
        AvailabilityPeriod newPeriod = new AvailabilityPeriod(startTime, endTime);
        
        // Check if this period overlaps with existing ones
        boolean overlaps = periods.stream().anyMatch(p -> p.overlaps(newPeriod));
        if (!overlaps) {
            periods.add(newPeriod);
            // Sort periods by start time
            periods.sort(Comparator.comparing(AvailabilityPeriod::getStartTime));
        }
    }

    /**
     * Remove availability for a specific day (removes all periods for that day)
     */
    public void removeAvailability(DayOfWeek day) {
        availability.remove(day);
    }
    
    /**
     * Remove availability for a specific time slot on a specific day
     * This splits existing availability periods as needed
     */
    public void removeTimeSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        List<AvailabilityPeriod> periods = availability.get(day);
        if (periods == null || periods.isEmpty()) {
            return;
        }
        
        List<AvailabilityPeriod> newPeriods = new ArrayList<>();
        
        for (AvailabilityPeriod period : periods) {
            // If no overlap, keep the period as is
            if (!period.overlaps(startTime, endTime)) {
                newPeriods.add(period);
                continue;
            }
            // If the period to remove is completely contained within this period, split it
            if (period.contains(startTime, endTime)) {
                // Add period before the removed slot
                if (period.getStartTime().isBefore(startTime)) {
                    newPeriods.add(new AvailabilityPeriod(period.getStartTime(), startTime));
                }
                // Add period after the removed slot
                if (period.getEndTime().isAfter(endTime)) {
                    newPeriods.add(new AvailabilityPeriod(endTime, period.getEndTime()));
                }
            }
            // If the removed slot partially overlaps at the start
            else if (period.getStartTime().isBefore(endTime) && period.getEndTime().isAfter(endTime)) {
                newPeriods.add(new AvailabilityPeriod(endTime, period.getEndTime()));
            }
            // If the removed slot partially overlaps at the end
            else if (period.getStartTime().isBefore(startTime) && period.getEndTime().isAfter(startTime)) {
                newPeriods.add(new AvailabilityPeriod(period.getStartTime(), startTime));
            }
            // Otherwise, the removed slot completely covers this period, so don't add it
        }
        
        availability.put(day, newPeriods);
    }
    
    /**
     * Remove availability for a specific TimeSlot
     */
    public void removeTimeSlot(TimeSlot timeSlot) {
        removeTimeSlot(timeSlot.getDay(), timeSlot.getStartTime(), timeSlot.getEndTime());
    }
    
    /**
     * Get all available time slots for a specific day
     */
    public List<AvailabilityPeriod> getAvailabilityForDay(DayOfWeek day) {
        List<AvailabilityPeriod> periods = availability.get(day);
        return periods != null ? new ArrayList<>(periods) : new ArrayList<>();
    }
    
    /**
     * Get all available time periods as a formatted string
     */
    public String getAvailabilityString() {
        StringBuilder sb = new StringBuilder();
        for (DayOfWeek day : DayOfWeek.values()) {
            List<AvailabilityPeriod> periods = availability.get(day);
            if (periods != null && !periods.isEmpty()) {
                sb.append(day).append(": ");
                sb.append(periods.stream()
                    .map(p -> p.getStartTime() + "-" + p.getEndTime())
                    .collect(Collectors.joining(", ")));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<DayOfWeek, List<AvailabilityPeriod>> getAvailability() {
        Map<DayOfWeek, List<AvailabilityPeriod>> copy = new HashMap<>();
        for (Map.Entry<DayOfWeek, List<AvailabilityPeriod>> entry : availability.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Professor professor = (Professor) o;
        return Objects.equals(id, professor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (ID: " + id + ")";
    }

    /**
     * Inner class representing an availability period
     */
    public static class AvailabilityPeriod {
        private LocalTime startTime;
        private LocalTime endTime;

        public AvailabilityPeriod(LocalTime startTime, LocalTime endTime) {
            if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
            this.startTime = startTime;
            this.endTime = endTime;
        }

        /**
         * Check if a time range is fully available within this period
         */
        public boolean isAvailable(LocalTime start, LocalTime end) {
            return !start.isBefore(startTime) && !end.isAfter(endTime);
        }
        
        /**
         * Check if this period overlaps with another time range
         */
        public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
            return this.startTime.isBefore(otherEnd) && this.endTime.isAfter(otherStart);
        }
        
        /**
         * Check if this period overlaps with another AvailabilityPeriod
         */
        public boolean overlaps(AvailabilityPeriod other) {
            return overlaps(other.startTime, other.endTime);
        }
        
        /**
         * Check if this period completely contains another time range
         */
        public boolean contains(LocalTime otherStart, LocalTime otherEnd) {
            return !this.startTime.isAfter(otherStart) && !this.endTime.isBefore(otherEnd);
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }
        
        @Override
        public String toString() {
            return startTime + "-" + endTime;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AvailabilityPeriod that = (AvailabilityPeriod) o;
            return Objects.equals(startTime, that.startTime) &&
                   Objects.equals(endTime, that.endTime);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(startTime, endTime);
        }
    }
}
