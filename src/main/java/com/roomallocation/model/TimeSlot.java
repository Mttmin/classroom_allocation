package com.roomallocation.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a specific time slot (day + time)
 */
public class TimeSlot implements Comparable<TimeSlot> {
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;

    public TimeSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    /**
     * Create a time slot with duration in minutes
     */
    public static TimeSlot fromDuration(DayOfWeek day, LocalTime startTime, int durationMinutes) {
        LocalTime endTime = startTime.plusMinutes(durationMinutes);
        return new TimeSlot(day, startTime, endTime);
    }

    /**
     * Check if this time slot overlaps with another
     */
    public boolean overlapsWith(TimeSlot other) {
        if (!this.day.equals(other.day)) {
            return false;
        }

        // Two time slots overlap if one starts before the other ends
        return this.startTime.isBefore(other.endTime) &&
               this.endTime.isAfter(other.startTime);
    }

    /**
     * Get duration in minutes
     */
    public int getDurationMinutes() {
        return (endTime.toSecondOfDay() - startTime.toSecondOfDay()) / 60;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return day == timeSlot.day &&
               Objects.equals(startTime, timeSlot.startTime) &&
               Objects.equals(endTime, timeSlot.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, startTime, endTime);
    }

    @Override
    public int compareTo(TimeSlot other) {
        int dayCompare = this.day.compareTo(other.day);
        if (dayCompare != 0) {
            return dayCompare;
        }
        int startCompare = this.startTime.compareTo(other.startTime);
        if (startCompare != 0) {
            return startCompare;
        }
        return this.endTime.compareTo(other.endTime);
    }

    @Override
    public String toString() {
        return day + " " + startTime + "-" + endTime;
    }
}
