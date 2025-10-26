package com.roomallocation.constraint;

import com.roomallocation.model.Schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Main validator that checks all constraints
 */
public class ConstraintValidator {
    private CorrelationConstraint correlationConstraint;
    private ProfessorConstraint professorConstraint;
    private RoomConstraint roomConstraint;

    public ConstraintValidator(double softCorrelationThreshold) {
        this.correlationConstraint = new CorrelationConstraint(softCorrelationThreshold);
        this.professorConstraint = new ProfessorConstraint();
        this.roomConstraint = new RoomConstraint();
    }

    /**
     * Validate all hard constraints
     * Returns true if all hard constraints are satisfied
     */
    public boolean validateHardConstraints(Schedule schedule) {
        return getAllViolations(schedule).isEmpty();
    }

    /**
     * Get all constraint violations
     */
    public List<String> getAllViolations(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        // Hard constraints
        violations.addAll(correlationConstraint.validateHardConstraints(schedule));
        violations.addAll(professorConstraint.validateConstraints(schedule));
        violations.addAll(roomConstraint.validateConstraints(schedule));

        return violations;
    }

    /**
     * Calculate total penalty for soft constraint violations
     */
    public double calculateTotalPenalty(Schedule schedule) {
        double penalty = 0.0;

        // Correlation soft penalty
        penalty += correlationConstraint.calculateSoftPenalty(schedule);

        // Professor schedule quality penalty
        penalty += professorConstraint.calculateScheduleQualityPenalty(schedule);

        return penalty;
    }

    /**
     * Print all violations to console
     */
    public void printViolations(Schedule schedule) {
        List<String> violations = getAllViolations(schedule);

        if (violations.isEmpty()) {
            System.out.println("No constraint violations found.");
        } else {
            System.out.println("Constraint violations found:");
            for (String violation : violations) {
                System.out.println("  - " + violation);
            }
        }
    }

    public CorrelationConstraint getCorrelationConstraint() {
        return correlationConstraint;
    }

    public ProfessorConstraint getProfessorConstraint() {
        return professorConstraint;
    }

    public RoomConstraint getRoomConstraint() {
        return roomConstraint;
    }
}
