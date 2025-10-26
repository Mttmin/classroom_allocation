package com.roomallocation.util;

import com.roomallocation.model.TimeSlot;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates possible time slots for course scheduling
 */
public class TimeSlotGenerator {
    /**
     * Generate all possible time slots between 8am-8pm
     * Time slots are 30-minute blocks
     */
    public static List<TimeSlot> generateTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();

        DayOfWeek[] workDays = {
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        };

        // Generate slots from 8:00 to 20:00 (8pm) with 30-minute duration
        for (DayOfWeek day : workDays) {
            for (int hour = 8; hour < 20; hour++) {
                LocalTime startTime = LocalTime.of(hour, 0);
                LocalTime endTime = startTime.plusMinutes(30);
                slots.add(new TimeSlot(day, startTime, endTime));
            }
        }

        return slots;
    }

    /**
     * Generate time slots with custom duration
     */
    public static List<TimeSlot> generateTimeSlotsWithDuration(int durationMinutes) {
        List<TimeSlot> slots = new ArrayList<>();

        DayOfWeek[] workDays = {
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        };

        for (DayOfWeek day : workDays) {
            for (int hour = 8; hour < 20; hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    LocalTime startTime = LocalTime.of(hour, minute);
                    LocalTime endTime = startTime.plusMinutes(durationMinutes);

                    // Only add if end time is before or at 20:00
                    if (!endTime.isAfter(LocalTime.of(20, 0))) {
                        slots.add(new TimeSlot(day, startTime, endTime));
                    }
                }
            }
        }

        return slots;
    }
}