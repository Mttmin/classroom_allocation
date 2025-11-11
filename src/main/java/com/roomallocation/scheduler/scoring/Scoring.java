package com.roomallocation.scheduler.scoring;

import com.roomallocation.model.*;
import com.roomallocation.scheduler.util.TimeSlotGenerator;
import java.time.Duration;
import java.util.*;

/**
 * Scoring system for evaluating schedule quality
 * Lower scores are better (penalties)
 */
public class Scoring {

    // Penalty weights (higher weight = more important)
    private double correlationPenaltyWeight = 1000.0;  // Very high - critical for student options
    private double professorGapPenaltyWeight = 10.0;
    private double timePreferencePenaltyWeight = 20.0;
    private double earlyClassPenaltyWeight = 10.0;

    // Thresholds
    private double correlationSoftThreshold = 0.3;  // Start penalizing above this correlation
    private int professorGapThresholdMinutes = 90;  // Gaps longer than this are penalized

    public Scoring() {
    }

    public Scoring(double correlationPenaltyWeight,
                   double professorGapPenaltyWeight,
                   double timePreferencePenaltyWeight,
                   double earlyClassPenaltyWeight) {
        this.correlationPenaltyWeight = correlationPenaltyWeight;
        this.professorGapPenaltyWeight = professorGapPenaltyWeight;
        this.timePreferencePenaltyWeight = timePreferencePenaltyWeight;
        this.earlyClassPenaltyWeight = earlyClassPenaltyWeight;
    }

    /**
     * Calculate total score for a schedule (lower is better)
     */
    public double calculateScore(Schedule schedule) {
        double score = 0.0;

        score += calculateCorrelationPenalty(schedule) * correlationPenaltyWeight;
        score += calculateProfessorGapPenalty(schedule) * professorGapPenaltyWeight;
        score += calculateTimePreferencePenalty(schedule) * timePreferencePenaltyWeight;
        score += calculateEarlyClassPenalty(schedule) * earlyClassPenaltyWeight;

        return score;
    }

    /**
     * Calculate penalty for student correlation overlaps
     * Goal: Minimize overlaps between courses with high student correlation
     * This gives students more options
     */
    public double calculateCorrelationPenalty(Schedule schedule) {
        double penalty = 0.0;
        List<ScheduledCourse> courses = schedule.getScheduledCourses();

        for (int i = 0; i < courses.size(); i++) {
            for (int j = i + 1; j < courses.size(); j++) {
                ScheduledCourse course1 = courses.get(i);
                ScheduledCourse course2 = courses.get(j);

                if (!course1.isScheduled() || !course2.isScheduled()) {
                    continue;
                }

                double correlation = schedule.getCorrelation(course1.getCourse(), course2.getCourse());

                // Penalize overlaps for correlated courses
                // (Hard constraint violations with correlation >= 2.0 should be caught by validator)
                if (correlation >= correlationSoftThreshold && correlation < 2.0) {
                    if (course1.hasTimeConflictWith(course2)) {
                        // Penalty scales exponentially with correlation
                        penalty += Math.pow(correlation, 2) * 100;
                    }
                }
            }
        }

        return penalty;
    }

    /**
     * Calculate penalty for gaps in professor schedules
     * Goal: Minimize gaps between classes for each professor
     */
    public double calculateProfessorGapPenalty(Schedule schedule) {
        double penalty = 0.0;
        Map<String, Professor> professors = schedule.getProfessors();

        for (Professor professor : professors.values()) {
            // Get all courses for this professor
            List<ScheduledCourse> professorCourses = schedule.getCoursesByProfessor(professor.getId());

            // Group sessions by day
            Map<java.time.DayOfWeek, List<TimeSlot>> sessionsByDay = new HashMap<>();

            for (ScheduledCourse sc : professorCourses) {
                if (!sc.isScheduled()) continue;

                for (TimeSlot slot : sc.getSessionPattern().getSessions()) {
                    sessionsByDay.computeIfAbsent(slot.getDay(), k -> new ArrayList<>()).add(slot);
                }
            }

            // Calculate gaps for each day
            for (List<TimeSlot> daySlots : sessionsByDay.values()) {
                if (daySlots.size() <= 1) continue;

                // Sort by start time
                daySlots.sort(Comparator.comparing(TimeSlot::getStartTime));

                // Calculate gaps between consecutive sessions
                for (int i = 0; i < daySlots.size() - 1; i++) {
                    TimeSlot current = daySlots.get(i);
                    TimeSlot next = daySlots.get(i + 1);

                    // Gap is from end of current to start of next
                    long gapMinutes = Duration.between(current.getEndTime(), next.getStartTime()).toMinutes();

                    if (gapMinutes > professorGapThresholdMinutes) {
                        // Penalty increases with gap size
                        penalty += (gapMinutes - professorGapThresholdMinutes) * 0.5;
                    }
                }
            }
        }

        return penalty;
    }

    /**
     * Calculate penalty for classes outside preferred hours (9am-5pm)
     * Goal: Prefer classes in 9-5 window (extended to 8am-8pm)
     */
    public double calculateTimePreferencePenalty(Schedule schedule) {
        double penalty = 0.0;

        for (ScheduledCourse sc : schedule.getScheduledCourses()) {
            if (!sc.isScheduled()) continue;

            for (TimeSlot slot : sc.getSessionPattern().getSessions()) {
                if (!TimeSlotGenerator.isPreferredTime(slot)) {
                    // Penalty for being outside preferred hours
                    // Calculate how far outside the preferred range
                    double hoursOutside = 0.0;

                    if (slot.getStartTime().isBefore(TimeSlotGenerator.PREFERRED_START)) {
                        hoursOutside += Duration.between(slot.getStartTime(), TimeSlotGenerator.PREFERRED_START).toMinutes() / 60.0;
                    }

                    if (slot.getEndTime().isAfter(TimeSlotGenerator.PREFERRED_END)) {
                        hoursOutside += Duration.between(TimeSlotGenerator.PREFERRED_END, slot.getEndTime()).toMinutes() / 60.0;
                    }

                    penalty += hoursOutside * 10;
                }
            }
        }

        return penalty;
    }

    /**
     * Calculate penalty for early classes (8am-10am)
     * Goal: Apply exponential malus for early classes (but not too heavy)
     */
    public double calculateEarlyClassPenalty(Schedule schedule) {
        double penalty = 0.0;

        for (ScheduledCourse sc : schedule.getScheduledCourses()) {
            if (!sc.isScheduled()) continue;

            for (TimeSlot slot : sc.getSessionPattern().getSessions()) {
                if (TimeSlotGenerator.isEarlyClass(slot)) {
                    double earlyHours = TimeSlotGenerator.getEarlyClassHours(slot);
                    // Exponential penalty: e^(earlyHours) - 1
                    // At 8am (2 hours early): e^2 - 1 ≈ 6.4
                    // At 9am (1 hour early): e^1 - 1 ≈ 1.7
                    // At 9:30am (0.5 hours early): e^0.5 - 1 ≈ 0.65
                    penalty += (Math.exp(earlyHours) - 1) * 5;
                }
            }
        }

        return penalty;
    }

    /**
     * Calculate individual score components for a schedule
     */
    public Map<String, Double> getScoreBreakdown(Schedule schedule) {
        Map<String, Double> breakdown = new LinkedHashMap<>();

        breakdown.put("Correlation Penalty", calculateCorrelationPenalty(schedule) * correlationPenaltyWeight);
        breakdown.put("Professor Gap Penalty", calculateProfessorGapPenalty(schedule) * professorGapPenaltyWeight);
        breakdown.put("Time Preference Penalty", calculateTimePreferencePenalty(schedule) * timePreferencePenaltyWeight);
        breakdown.put("Early Class Penalty", calculateEarlyClassPenalty(schedule) * earlyClassPenaltyWeight);
        breakdown.put("Total Score", calculateScore(schedule));

        return breakdown;
    }

    // Getters and setters
    public double getCorrelationPenaltyWeight() {
        return correlationPenaltyWeight;
    }

    public void setCorrelationPenaltyWeight(double correlationPenaltyWeight) {
        this.correlationPenaltyWeight = correlationPenaltyWeight;
    }

    public double getProfessorGapPenaltyWeight() {
        return professorGapPenaltyWeight;
    }

    public void setProfessorGapPenaltyWeight(double professorGapPenaltyWeight) {
        this.professorGapPenaltyWeight = professorGapPenaltyWeight;
    }

    public double getTimePreferencePenaltyWeight() {
        return timePreferencePenaltyWeight;
    }

    public void setTimePreferencePenaltyWeight(double timePreferencePenaltyWeight) {
        this.timePreferencePenaltyWeight = timePreferencePenaltyWeight;
    }

    public double getEarlyClassPenaltyWeight() {
        return earlyClassPenaltyWeight;
    }

    public void setEarlyClassPenaltyWeight(double earlyClassPenaltyWeight) {
        this.earlyClassPenaltyWeight = earlyClassPenaltyWeight;
    }

    public double getCorrelationSoftThreshold() {
        return correlationSoftThreshold;
    }

    public void setCorrelationSoftThreshold(double correlationSoftThreshold) {
        this.correlationSoftThreshold = correlationSoftThreshold;
    }

    @Override
    public String toString() {
        return String.format("Scoring[corr=%.1f, gap=%.1f, time=%.1f, early=%.1f]",
            correlationPenaltyWeight, professorGapPenaltyWeight,
            timePreferencePenaltyWeight, earlyClassPenaltyWeight);
    }
}
