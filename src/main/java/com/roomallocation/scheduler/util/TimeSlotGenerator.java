package com.roomallocation.scheduler.util;

import com.roomallocation.model.TimeSlot;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates all possible time slots for scheduling courses
 */
public class TimeSlotGenerator {
    // Default time bounds
    public static final LocalTime DEFAULT_START = LocalTime.of(8, 0); // 8am
    public static final LocalTime DEFAULT_END = LocalTime.of(20, 0);   // 8pm

    // Preferred time bounds (9am-5pm)
    public static final LocalTime PREFERRED_START = LocalTime.of(9, 0);
    public static final LocalTime PREFERRED_END = LocalTime.of(17, 0);

    // Early class penalty range (8am-10am)
    public static final LocalTime EARLY_CLASS_START = LocalTime.of(8, 0);
    public static final LocalTime EARLY_CLASS_END = LocalTime.of(10, 0);

    // Default working days
    public static final List<DayOfWeek> WORKING_DAYS = List.of(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    );

    // Time slot granularity (slots start every X minutes)
    private static final int SLOT_GRANULARITY_MINUTES = 30;

    private LocalTime startTime;
    private LocalTime endTime;
    private List<DayOfWeek> days;

    public TimeSlotGenerator() {
        this(DEFAULT_START, DEFAULT_END, WORKING_DAYS);
    }

    public TimeSlotGenerator(LocalTime startTime, LocalTime endTime, List<DayOfWeek> days) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.days = new ArrayList<>(days);
    }

    /**
     * Generate all possible time slots for a given duration
     * @param durationMinutes Duration of the course in minutes
     * @return List of all possible time slots
     */
    public List<TimeSlot> generateTimeSlots(int durationMinutes) {
        List<TimeSlot> slots = new ArrayList<>();

        for (DayOfWeek day : days) {
            LocalTime currentStart = startTime;

            // Generate slots starting every SLOT_GRANULARITY_MINUTES
            while (currentStart.plusMinutes(durationMinutes).isBefore(endTime) ||
                   currentStart.plusMinutes(durationMinutes).equals(endTime)) {

                LocalTime slotEnd = currentStart.plusMinutes(durationMinutes);
                slots.add(new TimeSlot(day, currentStart, slotEnd));

                currentStart = currentStart.plusMinutes(SLOT_GRANULARITY_MINUTES);
            }
        }

        return slots;
    }

    /**
     * Generate typical session patterns for a course
     * @param durationMinutes Duration of each session
     * @param sessionsPerWeek Number of sessions per week (e.g., 2, 3)
     * @return List of possible session patterns
     */
    public List<List<TimeSlot>> generateSessionPatterns(int durationMinutes, int sessionsPerWeek) {
        List<List<TimeSlot>> patterns = new ArrayList<>();

        // Generate all possible time slots
        List<TimeSlot> allSlots = generateTimeSlots(durationMinutes);

        if (sessionsPerWeek == 1) {
            // Single session: just return each slot as a pattern
            for (TimeSlot slot : allSlots) {
                List<TimeSlot> pattern = new ArrayList<>();
                pattern.add(slot);
                patterns.add(pattern);
            }
        } else if (sessionsPerWeek == 2) {
            // Two sessions per week: try all pairs with different days
            for (int i = 0; i < allSlots.size(); i++) {
                TimeSlot slot1 = allSlots.get(i);
                for (int j = i + 1; j < allSlots.size(); j++) {
                    TimeSlot slot2 = allSlots.get(j);

                    // Must be different days
                    if (!slot1.getDay().equals(slot2.getDay())) {
                        List<TimeSlot> pattern = new ArrayList<>();
                        pattern.add(slot1);
                        pattern.add(slot2);
                        patterns.add(pattern);
                    }
                }
            }
        } else if (sessionsPerWeek == 3) {
            // Three sessions per week: try Mon/Wed/Fri or Tue/Thu + one more
            // For efficiency, prefer same time on different days
            for (TimeSlot slot : allSlots) {
                // Mon/Wed/Fri pattern (same time)
                if (slot.getDay() == DayOfWeek.MONDAY) {
                    List<TimeSlot> pattern = new ArrayList<>();
                    pattern.add(slot);
                    pattern.add(new TimeSlot(DayOfWeek.WEDNESDAY, slot.getStartTime(), slot.getEndTime()));
                    pattern.add(new TimeSlot(DayOfWeek.FRIDAY, slot.getStartTime(), slot.getEndTime()));
                    patterns.add(pattern);
                }

                // Tue/Wed/Thu pattern (same time)
                if (slot.getDay() == DayOfWeek.TUESDAY) {
                    List<TimeSlot> pattern = new ArrayList<>();
                    pattern.add(slot);
                    pattern.add(new TimeSlot(DayOfWeek.WEDNESDAY, slot.getStartTime(), slot.getEndTime()));
                    pattern.add(new TimeSlot(DayOfWeek.THURSDAY, slot.getStartTime(), slot.getEndTime()));
                    patterns.add(pattern);
                }
            }
        } else {
            // For other session counts, generate patterns with same time on consecutive days
            for (TimeSlot slot : allSlots) {
                List<TimeSlot> pattern = new ArrayList<>();
                pattern.add(slot);

                // Add same time on next N-1 days
                int dayIndex = days.indexOf(slot.getDay());
                for (int i = 1; i < sessionsPerWeek && (dayIndex + i) < days.size(); i++) {
                    DayOfWeek nextDay = days.get(dayIndex + i);
                    pattern.add(new TimeSlot(nextDay, slot.getStartTime(), slot.getEndTime()));
                }

                if (pattern.size() == sessionsPerWeek) {
                    patterns.add(pattern);
                }
            }
        }

        return patterns;
    }

    /**
     * Check if a time slot is in preferred hours (9am-5pm)
     */
    public static boolean isPreferredTime(TimeSlot slot) {
        return !slot.getStartTime().isBefore(PREFERRED_START) &&
               !slot.getEndTime().isAfter(PREFERRED_END);
    }

    /**
     * Check if a time slot is an early class (starts before 10am)
     */
    public static boolean isEarlyClass(TimeSlot slot) {
        return slot.getStartTime().isBefore(EARLY_CLASS_END);
    }

    /**
     * Get how early a class starts (in hours before 10am)
     * Returns 0 if not an early class
     */
    public static double getEarlyClassHours(TimeSlot slot) {
        if (!isEarlyClass(slot)) {
            return 0.0;
        }

        int minutesBeforeTen = (EARLY_CLASS_END.toSecondOfDay() - slot.getStartTime().toSecondOfDay()) / 60;
        return minutesBeforeTen / 60.0;
    }
}
