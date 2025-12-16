package com.roomallocation.statistics;

import com.roomallocation.model.Schedule;
import com.roomallocation.model.ScheduledCourse;
import com.roomallocation.model.SessionPattern;
import com.roomallocation.model.TimeSlot;
import com.roomallocation.scheduler.scoring.Scoring;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive statistics for scheduler performance and quality metrics
 */
public class SchedulerStatistics {

    private final Schedule schedule;
    private final Scoring scoring;

    // Basic metrics
    private int totalCourses;
    private int scheduledCourses;
    private int unscheduledCourses;
    private double schedulingRate;

    // Score breakdown
    private double totalScore;
    private Map<String, Double> scoreBreakdown;

    // Time distribution
    private Map<String, Integer> timeSlotDistribution; // Morning/Afternoon/Evening
    private Map<DayOfWeek, Integer> dayDistribution;
    private Map<String, Integer> patternDistribution; // MWF, TR, etc.

    // Professor metrics
    private double averageGapMinutes;
    private double maxGapMinutes;
    private double averageTeachingHoursPerProfessor;
    private Map<String, Double> professorWorkloadDistribution;

    // Constraint metrics
    private int hardConstraintViolations;
    private int correlationConflicts;
    private int professorAvailabilityConflicts;

    // Time preference metrics
    private int coursesInPreferredTimeWindow; // 8am-8pm
    private int coursesInBusinessHours; // 9am-5pm
    private int coursesEarlyMorning; // 8am-10am
    private int earlyMorningCourses; // Before 10am
    private double averageStartTime; // In hours (e.g., 10.5 = 10:30am)

    // Room utilization
    private Map<String, Double> roomUtilizationRate;

    // Execution metrics
    private long executionTimeMs;

    public SchedulerStatistics(Schedule schedule, Scoring scoring) {
        this.schedule = schedule;
        this.scoring = scoring;
        this.scoreBreakdown = new HashMap<>();
        this.timeSlotDistribution = new HashMap<>();
        this.dayDistribution = new HashMap<>();
        this.patternDistribution = new HashMap<>();
        this.professorWorkloadDistribution = new HashMap<>();
        this.roomUtilizationRate = new HashMap<>();

        calculateStatistics();
    }

    private void calculateStatistics() {
        List<ScheduledCourse> allCourses = schedule.getScheduledCourses();
        List<ScheduledCourse> scheduled = allCourses.stream()
            .filter(ScheduledCourse::isScheduled)
            .collect(Collectors.toList());

        this.totalCourses = allCourses.size();
        this.scheduledCourses = scheduled.size();
        this.unscheduledCourses = totalCourses - scheduledCourses;
        this.schedulingRate = totalCourses > 0 ? (double) scheduledCourses / totalCourses : 0.0;

        // Calculate scores
        calculateScores();

        // Calculate time distributions
        calculateTimeDistributions(scheduled);

        // Calculate professor metrics
        calculateProfessorMetrics(scheduled);

        // Calculate constraint violations
        calculateConstraintViolations(scheduled);

        // Calculate time preferences
        calculateTimePreferences(scheduled);

        // Calculate room utilization
        calculateRoomUtilization(scheduled);
    }

    private void calculateScores() {
        if (scoring != null && schedule != null) {
            this.scoreBreakdown = scoring.getScoreBreakdown(schedule);
            this.totalScore = scoreBreakdown.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        }
    }

    private void calculateTimeDistributions(List<ScheduledCourse> scheduled) {
        timeSlotDistribution.put("Morning (6am-12pm)", 0);
        timeSlotDistribution.put("Afternoon (12pm-5pm)", 0);
        timeSlotDistribution.put("Evening (5pm-10pm)", 0);

        for (DayOfWeek day : DayOfWeek.values()) {
            dayDistribution.put(day, 0);
        }

        Map<String, Integer> patternCounts = new HashMap<>();

        for (ScheduledCourse sc : scheduled) {
            if (sc.getSessionPattern() != null) {
                SessionPattern pattern = sc.getSessionPattern();

                // Count by time of day
                for (TimeSlot slot : pattern.getSessions()) {
                    LocalTime start = slot.getStartTime();

                    if (start.isBefore(LocalTime.of(12, 0))) {
                        timeSlotDistribution.merge("Morning (6am-12pm)", 1, Integer::sum);
                    } else if (start.isBefore(LocalTime.of(17, 0))) {
                        timeSlotDistribution.merge("Afternoon (12pm-5pm)", 1, Integer::sum);
                    } else {
                        timeSlotDistribution.merge("Evening (5pm-10pm)", 1, Integer::sum);
                    }

                    // Count by day
                    dayDistribution.merge(slot.getDay(), 1, Integer::sum);
                }

                // Count by pattern (e.g., MWF, TR)
                String patternKey = pattern.getSessions().stream()
                    .map(ts -> ts.getDay().toString().substring(0, 1))
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining());
                patternCounts.merge(patternKey, 1, Integer::sum);
            }
        }

        this.patternDistribution = patternCounts;
    }

    private void calculateProfessorMetrics(List<ScheduledCourse> scheduled) {
        Map<String, List<TimeSlot>> professorSchedules = new HashMap<>();

        // Group time slots by professor
        for (ScheduledCourse sc : scheduled) {
            if (sc.getSessionPattern() != null) {
                for (String professorId : sc.getCourse().getProfessorIds()) {
                    professorSchedules.computeIfAbsent(professorId, k -> new ArrayList<>())
                        .addAll(sc.getSessionPattern().getSessions());
                }
            }
        }

        double totalGaps = 0;
        double maxGap = 0;
        double totalTeachingHours = 0;

        for (Map.Entry<String, List<TimeSlot>> entry : professorSchedules.entrySet()) {
            String profId = entry.getKey();
            List<TimeSlot> slots = entry.getValue();

            // Calculate teaching hours for this professor
            double teachingHours = slots.stream()
                .mapToDouble(ts -> Duration.between(ts.getStartTime(), ts.getEndTime()).toMinutes())
                .sum() / 60.0;

            totalTeachingHours += teachingHours;
            professorWorkloadDistribution.put(profId, teachingHours);

            // Calculate gaps for each day
            Map<DayOfWeek, List<TimeSlot>> slotsByDay = slots.stream()
                .collect(Collectors.groupingBy(TimeSlot::getDay));

            for (List<TimeSlot> daySlots : slotsByDay.values()) {
                if (daySlots.size() > 1) {
                    // Sort by start time
                    daySlots.sort(Comparator.comparing(TimeSlot::getStartTime));

                    for (int i = 0; i < daySlots.size() - 1; i++) {
                        TimeSlot current = daySlots.get(i);
                        TimeSlot next = daySlots.get(i + 1);

                        long gapMinutes = Duration.between(current.getEndTime(), next.getStartTime()).toMinutes();
                        if (gapMinutes > 0) {
                            totalGaps += gapMinutes;
                            maxGap = Math.max(maxGap, gapMinutes);
                        }
                    }
                }
            }
        }

        int professorCount = professorSchedules.size();
        this.averageGapMinutes = professorCount > 0 ? totalGaps / professorCount : 0;
        this.maxGapMinutes = maxGap;
        this.averageTeachingHoursPerProfessor = professorCount > 0 ? totalTeachingHours / professorCount : 0;
    }

    private void calculateConstraintViolations(List<ScheduledCourse> scheduled) {
        // This would need access to the correlation matrix and professor availability
        // For now, we'll estimate based on the score
        this.hardConstraintViolations = 0; // Would need actual validation logic
        this.correlationConflicts = 0;
        this.professorAvailabilityConflicts = 0;

        // If correlation penalty is high, we have conflicts
        if (scoreBreakdown.containsKey("correlationPenalty")) {
            double correlationPenalty = scoreBreakdown.get("correlationPenalty");
            // Estimate conflicts (penalty weight is 1000 per conflict)
            this.correlationConflicts = (int) (correlationPenalty / 1000);
        }
    }

    private void calculateTimePreferences(List<ScheduledCourse> scheduled) {
        this.coursesInPreferredTimeWindow = 0;
        this.coursesInBusinessHours = 0;
        this.coursesEarlyMorning = 0;
        this.earlyMorningCourses = 0;

        double totalStartTimeHours = 0;
        int slotCount = 0;

        LocalTime preferredStart = LocalTime.of(8, 0);   // 8am
        LocalTime preferredEnd = LocalTime.of(20, 0);    // 8pm
        LocalTime businessStart = LocalTime.of(9, 0);    // 9am
        LocalTime businessEnd = LocalTime.of(17, 0);     // 5pm
        LocalTime earlyMorningStart = LocalTime.of(8, 0); // 8am
        LocalTime earlyMorningEnd = LocalTime.of(10, 0);  // 10am
        LocalTime earlyThreshold = LocalTime.of(10, 0);   // Before 10am

        for (ScheduledCourse sc : scheduled) {
            if (sc.getSessionPattern() != null) {
                for (TimeSlot slot : sc.getSessionPattern().getSessions()) {
                    LocalTime start = slot.getStartTime();

                    // 8am-8pm window
                    if (!start.isBefore(preferredStart) && start.isBefore(preferredEnd)) {
                        coursesInPreferredTimeWindow++;
                    }

                    // 9am-5pm business hours
                    if (!start.isBefore(businessStart) && start.isBefore(businessEnd)) {
                        coursesInBusinessHours++;
                    }

                    // 8am-10am early morning
                    if (!start.isBefore(earlyMorningStart) && start.isBefore(earlyMorningEnd)) {
                        coursesEarlyMorning++;
                    }

                    // Before 10am
                    if (start.isBefore(earlyThreshold)) {
                        earlyMorningCourses++;
                    }

                    // Calculate average start time
                    totalStartTimeHours += start.getHour() + start.getMinute() / 60.0;
                    slotCount++;
                }
            }
        }

        this.averageStartTime = slotCount > 0 ? totalStartTimeHours / slotCount : 0;
    }

    private void calculateRoomUtilization(List<ScheduledCourse> scheduled) {
        // Calculate how much each room is used
        Map<String, Integer> roomSlotCounts = new HashMap<>();

        for (ScheduledCourse sc : scheduled) {
            if (sc.getAssignedRoomId() != null && sc.getSessionPattern() != null) {
                int slots = sc.getSessionPattern().getSessions().size();
                roomSlotCounts.merge(sc.getAssignedRoomId(), slots, Integer::sum);
            }
        }

        // Calculate utilization rate (assuming 5 days, 8am-8pm = 60 possible 1-hour slots per week)
        int maxSlotsPerWeek = 60;
        for (Map.Entry<String, Integer> entry : roomSlotCounts.entrySet()) {
            double utilization = (double) entry.getValue() / maxSlotsPerWeek;
            roomUtilizationRate.put(entry.getKey(), utilization);
        }
    }

    // Getters
    public int getTotalCourses() { return totalCourses; }
    public int getScheduledCourses() { return scheduledCourses; }
    public int getUnscheduledCourses() { return unscheduledCourses; }
    public double getSchedulingRate() { return schedulingRate; }
    public double getTotalScore() { return totalScore; }
    public Map<String, Double> getScoreBreakdown() { return scoreBreakdown; }
    public Map<String, Integer> getTimeSlotDistribution() { return timeSlotDistribution; }
    public Map<DayOfWeek, Integer> getDayDistribution() { return dayDistribution; }
    public Map<String, Integer> getPatternDistribution() { return patternDistribution; }
    public double getAverageGapMinutes() { return averageGapMinutes; }
    public double getMaxGapMinutes() { return maxGapMinutes; }
    public double getAverageTeachingHoursPerProfessor() { return averageTeachingHoursPerProfessor; }
    public Map<String, Double> getProfessorWorkloadDistribution() { return professorWorkloadDistribution; }
    public int getHardConstraintViolations() { return hardConstraintViolations; }
    public int getCorrelationConflicts() { return correlationConflicts; }
    public int getProfessorAvailabilityConflicts() { return professorAvailabilityConflicts; }
    public int getCoursesInPreferredTimeWindow() { return coursesInPreferredTimeWindow; }
    public int getCoursesInBusinessHours() { return coursesInBusinessHours; }
    public int getCoursesEarlyMorning() { return coursesEarlyMorning; }
    public int getEarlyMorningCourses() { return earlyMorningCourses; }
    public double getAverageStartTime() { return averageStartTime; }
    public Map<String, Double> getRoomUtilizationRate() { return roomUtilizationRate; }
    public long getExecutionTimeMs() { return executionTimeMs; }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    /**
     * Convert statistics to a map for easy export
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();

        // Basic metrics
        map.put("totalCourses", totalCourses);
        map.put("scheduledCourses", scheduledCourses);
        map.put("unscheduledCourses", unscheduledCourses);
        map.put("schedulingRate", String.format("%.2f%%", schedulingRate * 100));

        // Scores
        map.put("totalScore", String.format("%.2f", totalScore));
        map.put("scoreBreakdown", scoreBreakdown);

        // Time distributions
        map.put("timeSlotDistribution", timeSlotDistribution);
        map.put("dayDistribution", dayDistribution);
        map.put("patternDistribution", patternDistribution);

        // Professor metrics
        map.put("averageGapMinutes", String.format("%.2f", averageGapMinutes));
        map.put("maxGapMinutes", String.format("%.2f", maxGapMinutes));
        map.put("averageTeachingHours", String.format("%.2f", averageTeachingHoursPerProfessor));
        map.put("professorWorkload", professorWorkloadDistribution);

        // Constraints
        map.put("hardConstraintViolations", hardConstraintViolations);
        map.put("correlationConflicts", correlationConflicts);
        map.put("professorAvailabilityConflicts", professorAvailabilityConflicts);

        // Time preferences
        map.put("coursesInPreferredTime", coursesInPreferredTimeWindow);
        map.put("coursesInBusinessHours", coursesInBusinessHours);
        map.put("coursesEarlyMorning", coursesEarlyMorning);
        map.put("earlyMorningCourses", earlyMorningCourses);
        map.put("averageStartTime", String.format("%.2f", averageStartTime));

        // Room utilization
        map.put("roomUtilization", roomUtilizationRate);

        // Performance
        map.put("executionTimeMs", executionTimeMs);

        return map;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Scheduler Statistics ===\n\n");

        sb.append("Basic Metrics:\n");
        sb.append(String.format("  Total Courses: %d\n", totalCourses));
        sb.append(String.format("  Scheduled: %d (%.2f%%)\n", scheduledCourses, schedulingRate * 100));
        sb.append(String.format("  Unscheduled: %d\n", unscheduledCourses));
        sb.append(String.format("  Total Score: %.2f (lower is better)\n", totalScore));
        sb.append(String.format("  Execution Time: %d ms\n\n", executionTimeMs));

        sb.append("Score Breakdown:\n");
        scoreBreakdown.forEach((key, value) ->
            sb.append(String.format("  %s: %.2f\n", key, value))
        );
        sb.append("\n");

        sb.append("Time Distribution:\n");
        timeSlotDistribution.forEach((key, value) ->
            sb.append(String.format("  %s: %d\n", key, value))
        );
        sb.append("\n");

        sb.append("Day Distribution:\n");
        dayDistribution.forEach((key, value) ->
            sb.append(String.format("  %s: %d sessions\n", key, value))
        );
        sb.append("\n");

        sb.append("Session Patterns:\n");
        patternDistribution.forEach((key, value) ->
            sb.append(String.format("  %s: %d courses\n", key, value))
        );
        sb.append("\n");

        sb.append("Professor Metrics:\n");
        sb.append(String.format("  Average Gap: %.2f minutes\n", averageGapMinutes));
        sb.append(String.format("  Max Gap: %.2f minutes\n", maxGapMinutes));
        sb.append(String.format("  Average Teaching Hours: %.2f hours/week\n\n", averageTeachingHoursPerProfessor));

        sb.append("Constraint Violations:\n");
        sb.append(String.format("  Correlation Conflicts: %d\n", correlationConflicts));
        sb.append(String.format("  Professor Availability Conflicts: %d\n\n", professorAvailabilityConflicts));

        sb.append("Time Preferences:\n");
        sb.append(String.format("  Courses in Preferred Window (8am-8pm): %d\n", coursesInPreferredTimeWindow));
        sb.append(String.format("  Courses in Business Hours (9am-5pm): %d\n", coursesInBusinessHours));
        sb.append(String.format("  Early Morning Courses (8am-10am): %d\n", coursesEarlyMorning));
        sb.append(String.format("  All Early Courses (<10am): %d\n", earlyMorningCourses));
        sb.append(String.format("  Average Start Time: %.2f (%.0f:%02d)\n",
            averageStartTime,
            Math.floor(averageStartTime),
            (int)((averageStartTime - Math.floor(averageStartTime)) * 60)
        ));

        return sb.toString();
    }
}
