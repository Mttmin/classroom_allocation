package com.roomallocation.constraint;

import com.roomallocation.model.Schedule;
import com.roomallocation.model.ScheduledCourse;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates that courses with high student correlation don't have time conflicts
 */
public class CorrelationConstraint {
    private static final double HARD_CONSTRAINT_THRESHOLD = 2.0; // Same program, cannot overlap
    private double softConstraintThreshold;

    public CorrelationConstraint(double softConstraintThreshold) {
        this.softConstraintThreshold = softConstraintThreshold;
    }

    /**
     * Check for hard constraint violations (correlation = 2.0)
     * Returns list of violation messages
     */
    public List<String> validateHardConstraints(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        List<ScheduledCourse> courses = schedule.getScheduledCourses();

        for (int i = 0; i < courses.size(); i++) {
            for (int j = i + 1; j < courses.size(); j++) {
                ScheduledCourse course1 = courses.get(i);
                ScheduledCourse course2 = courses.get(j);

                if (!course1.isScheduled() || !course2.isScheduled()) {
                    continue;
                }

                double correlation = schedule.getCorrelation(course1.getCourse(), course2.getCourse());

                // Hard constraint: courses with correlation = 2.0 cannot overlap
                if (correlation >= HARD_CONSTRAINT_THRESHOLD) {
                    if (course1.hasTimeConflictWith(course2)) {
                        violations.add(String.format(
                            "HARD CONSTRAINT VIOLATION: %s and %s have same-program correlation (%.1f) but overlap in time",
                            course1.getCourse().getName(),
                            course2.getCourse().getName(),
                            correlation
                        ));
                    }
                }
            }
        }

        return violations;
    }

    /**
     * Calculate penalty for soft constraint violations
     * Higher correlation = higher penalty for time conflicts
     */
    public double calculateSoftPenalty(Schedule schedule) {
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

                // Soft penalty: proportional to correlation and overlap
                if (correlation >= softConstraintThreshold && correlation < HARD_CONSTRAINT_THRESHOLD) {
                    if (course1.hasTimeConflictWith(course2)) {
                        // Penalty increases with correlation level
                        penalty += correlation * 100; // Scale factor
                    }
                }
            }
        }

        return penalty;
    }

    /**
     * Check if two specific courses violate constraints
     */
    public boolean violatesConstraint(ScheduledCourse course1, ScheduledCourse course2, Schedule schedule) {
        if (!course1.isScheduled() || !course2.isScheduled()) {
            return false;
        }

        double correlation = schedule.getCorrelation(course1.getCourse(), course2.getCourse());

        // Hard constraint violation
        if (correlation >= HARD_CONSTRAINT_THRESHOLD) {
            return course1.hasTimeConflictWith(course2);
        }

        return false;
    }

    public double getSoftConstraintThreshold() {
        return softConstraintThreshold;
    }

    public void setSoftConstraintThreshold(double threshold) {
        this.softConstraintThreshold = threshold;
    }
}
