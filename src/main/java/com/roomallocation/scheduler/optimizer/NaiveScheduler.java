package com.roomallocation.scheduler.optimizer;

import com.roomallocation.scheduler.scoring.Scoring;
import com.roomallocation.scheduler.util.TimeSlotGenerator;
import com.roomallocation.allocation.TypeBasedAllocation;
import com.roomallocation.constraint.ConstraintValidator;
import com.roomallocation.model.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Naive greedy time scheduler
 * Priority order:
 * 1. Hard constraints (correlation >= 2.0, professor availability)
 * 2. Student correlation minimization
 * 3. Room type satisfaction (via TypeBasedAllocation)
 * 4. Professor time slot grouping
 * 5. 9-5 preference (extended to 8am-8pm)
 * 6. Early class malus
 */
public class NaiveScheduler extends Scheduler {

    private TimeSlotGenerator timeSlotGenerator;
    private Map<String, Professor> professors;
    private double[][] correlationMatrix;
    private Random random;

    public NaiveScheduler(String name,
                          Scoring scoring,
                          ConstraintValidator constraints,
                          List<Course> courses,
                          List<Room> rooms,
                          TypeBasedAllocation allocator,
                          boolean forcereassign,
                          Map<String, Professor> professors,
                          double[][] correlationMatrix) {
        super(name, scoring, constraints, courses, rooms, allocator, forcereassign);
        this.professors = professors;
        this.correlationMatrix = correlationMatrix;
        this.timeSlotGenerator = new TimeSlotGenerator();
        this.random = new Random();
    }

    @Override
    public void runSchedule() {
        long startTime = System.currentTimeMillis();
        System.out.println("Running Naive Greedy Time Scheduler...");

        // Initialize schedule
        this.schedule = new Schedule(getCourses(), professors, correlationMatrix);

        // Get courses sorted by priority
        List<ScheduledCourse> prioritizedCourses = prioritizeCourses();

        int scheduledCount = 0;
        int totalCourses = prioritizedCourses.size();

        // Schedule each course greedily
        for (ScheduledCourse scheduledCourse : prioritizedCourses) {
            System.out.println(String.format("Scheduling %d/%d: %s",
                scheduledCount + 1, totalCourses, scheduledCourse.getCourse().getName()));

            // Try to find a valid session pattern
            SessionPattern bestPattern = findBestSessionPattern(scheduledCourse);

            if (bestPattern != null && !bestPattern.isEmpty()) {
                scheduledCourse.assignSessionPattern(bestPattern);
                scheduledCount++;
                System.out.println("  ✓ Scheduled: " + bestPattern);
            } else {
                System.out.println("  ✗ Failed to schedule (no valid pattern found)");
            }
        }

        // Calculate and set score
        double score = getScoring().calculateScore(schedule);
        schedule.setScore(score);

        long endTime = System.currentTimeMillis();
        this.executionTimeMs = endTime - startTime;

        System.out.println(String.format("\nTime Scheduling Complete: %d/%d courses scheduled",
            scheduledCount, totalCourses));
        System.out.println("Schedule Score: " + String.format("%.2f", score));
        System.out.println("Execution Time: " + executionTimeMs + " ms");

        // Print score breakdown
        Map<String, Double> breakdown = getScoring().getScoreBreakdown(schedule);
        System.out.println("\nScore Breakdown:");
        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            System.out.println(String.format("  %s: %.2f", entry.getKey(), entry.getValue()));
        }

        // Validate hard constraints
        List<String> violations = getConstraints().getAllViolations(schedule);
        if (!violations.isEmpty()) {
            System.out.println("\nWARNING: Hard constraint violations found:");
            for (String violation : violations) {
                System.out.println("  - " + violation);
            }
        }

        // Run room allocation
        System.out.println("\nRunning Room Allocation...");
        runRoomAllocation();

        this.scheduled = true;
    }

    /**
     * Prioritize courses for scheduling
     * Courses with higher correlation sums are scheduled first
     */
    private List<ScheduledCourse> prioritizeCourses() {
        List<ScheduledCourse> courses = new ArrayList<>(schedule.getScheduledCourses());

        // Calculate correlation sum for each course
        Map<String, Double> correlationSums = new HashMap<>();
        for (ScheduledCourse sc : courses) {
            double sum = 0.0;
            for (ScheduledCourse other : courses) {
                if (!sc.equals(other)) {
                    sum += schedule.getCorrelation(sc.getCourse(), other.getCourse());
                }
            }
            correlationSums.put(sc.getCourse().getName(), sum);
        }

        // Sort by correlation sum (descending) - higher correlation courses first
        courses.sort((a, b) -> {
            double sumA = correlationSums.get(a.getCourse().getName());
            double sumB = correlationSums.get(b.getCourse().getName());
            return Double.compare(sumB, sumA);
        });

        return courses;
    }

    /**
     * Find the best session pattern for a course
     */
    private SessionPattern findBestSessionPattern(ScheduledCourse scheduledCourse) {
        Course course = scheduledCourse.getCourse();
        int durationMinutes = course.getDurationMinutes();

        // Determine sessions per week based on duration
        // Typical: 60-90 min = 2-3 sessions, 120 min = 2 sessions, 180-200 min = 1 session
        int sessionsPerWeek = calculateSessionsPerWeek(durationMinutes);

        // Generate candidate session patterns
        List<List<TimeSlot>> candidatePatterns = timeSlotGenerator.generateSessionPatterns(
            durationMinutes, sessionsPerWeek);

        // If too many candidates, sample a subset for efficiency
        if (candidatePatterns.size() > 100) {
            candidatePatterns = samplePatterns(candidatePatterns, 100);
        }

        SessionPattern bestPattern = null;
        double bestScore = Double.POSITIVE_INFINITY;

        // Try each candidate pattern
        for (List<TimeSlot> patternSlots : candidatePatterns) {
            SessionPattern pattern = createSessionPattern(patternSlots);

            // Check hard constraints
            if (!isPatternValid(scheduledCourse, pattern)) {
                continue;
            }

            // Calculate score for this pattern
            double score = scorePattern(scheduledCourse, pattern);

            if (score < bestScore) {
                bestScore = score;
                bestPattern = pattern;
            }
        }

        return bestPattern;
    }

    /**
     * Calculate sessions per week based on duration
     */
    private int calculateSessionsPerWeek(int durationMinutes) {
        if (durationMinutes <= 90) {
            return 3; // Three 60-90 min sessions per week
        } else if (durationMinutes <= 120) {
            return 2; // Two 120 min sessions per week
        } else {
            return 1; // One long session per week (180-200 min)
        }
    }

    /**
     * Sample a subset of patterns randomly
     */
    private List<List<TimeSlot>> samplePatterns(List<List<TimeSlot>> patterns, int sampleSize) {
        if (patterns.size() <= sampleSize) {
            return patterns;
        }

        List<List<TimeSlot>> sampled = new ArrayList<>(patterns);
        Collections.shuffle(sampled, random);
        return sampled.subList(0, sampleSize);
    }

    /**
     * Create SessionPattern from list of TimeSlots
     */
    private SessionPattern createSessionPattern(List<TimeSlot> slots) {
        SessionPattern pattern = new SessionPattern();
        for (TimeSlot slot : slots) {
            pattern.addSession(slot);
        }
        return pattern;
    }

    /**
     * Check if a pattern is valid (hard constraints)
     */
    private boolean isPatternValid(ScheduledCourse scheduledCourse, SessionPattern pattern) {
        Course course = scheduledCourse.getCourse();

        // 1. Check professor availability
        for (String profId : course.getProfessorIds()) {
            Professor professor = professors.get(profId);
            if (professor != null && !pattern.fitsAvailability(professor)) {
                return false;
            }
        }

        // 2. Check for conflicts with already scheduled courses
        for (ScheduledCourse other : schedule.getScheduledCourses()) {
            if (other.equals(scheduledCourse) || !other.isScheduled()) {
                continue;
            }

            // Hard constraint: courses with correlation >= 2.0 cannot overlap
            double correlation = schedule.getCorrelation(course, other.getCourse());
            if (correlation >= 2.0) {
                // Create temporary session pattern to test overlap
                SessionPattern testPattern = pattern;
                if (testPattern.hasOverlapWith(other.getSessionPattern())) {
                    return false; // Hard constraint violation
                }
            }

            // Check professor conflicts
            if (!Collections.disjoint(course.getProfessorIds(), other.getCourse().getProfessorIds())) {
                if (pattern.hasOverlapWith(other.getSessionPattern())) {
                    return false; // Professor cannot teach two courses at once
                }
            }
        }

        return true;
    }

    /**
     * Score a pattern (lower is better)
     * This is a partial score considering only this course's contribution
     */
    private double scorePattern(ScheduledCourse scheduledCourse, SessionPattern pattern) {
        double score = 0.0;
        Course course = scheduledCourse.getCourse();

        // 1. Correlation penalty - check conflicts with already scheduled courses
        for (ScheduledCourse other : schedule.getScheduledCourses()) {
            if (other.equals(scheduledCourse) || !other.isScheduled()) {
                continue;
            }

            double correlation = schedule.getCorrelation(course, other.getCourse());
            if (correlation >= 0.5 && correlation < 2.0) { // Soft threshold
                if (pattern.hasOverlapWith(other.getSessionPattern())) {
                    score += Math.pow(correlation, 2) * 100 * 1000.0; // Weight
                }
            }
        }

        // 2. Professor gap penalty (if professor has other courses)
        for (String profId : course.getProfessorIds()) {
            Professor professor = professors.get(profId);
            if (professor != null) {
                score += calculateProfessorGapForPattern(professor, pattern);
            }
        }

        // 3. Time preference penalty
        for (TimeSlot slot : pattern.getSessions()) {
            if (!TimeSlotGenerator.isPreferredTime(slot)) {
                score += 50.0; // Penalty for non-preferred times
            }
        }

        // 4. Early class penalty
        for (TimeSlot slot : pattern.getSessions()) {
            if (TimeSlotGenerator.isEarlyClass(slot)) {
                double earlyHours = TimeSlotGenerator.getEarlyClassHours(slot);
                score += (Math.exp(earlyHours) - 1) * 5 * 20.0; // Weight
            }
        }

        return score;
    }

    /**
     * Calculate professor gap penalty for a new pattern
     */
    private double calculateProfessorGapForPattern(Professor professor, SessionPattern newPattern) {
        double penalty = 0.0;

        // Get existing courses for this professor
        List<ScheduledCourse> professorCourses = schedule.getCoursesByProfessor(professor.getId())
            .stream()
            .filter(ScheduledCourse::isScheduled)
            .collect(Collectors.toList());

        // Group all sessions (existing + new) by day
        Map<java.time.DayOfWeek, List<TimeSlot>> sessionsByDay = new HashMap<>();

        // Add existing sessions
        for (ScheduledCourse sc : professorCourses) {
            for (TimeSlot slot : sc.getSessionPattern().getSessions()) {
                sessionsByDay.computeIfAbsent(slot.getDay(), k -> new ArrayList<>()).add(slot);
            }
        }

        // Add new sessions
        for (TimeSlot slot : newPattern.getSessions()) {
            sessionsByDay.computeIfAbsent(slot.getDay(), k -> new ArrayList<>()).add(slot);
        }

        // Calculate gaps for each day
        for (List<TimeSlot> daySlots : sessionsByDay.values()) {
            if (daySlots.size() <= 1) continue;

            daySlots.sort(Comparator.comparing(TimeSlot::getStartTime));

            for (int i = 0; i < daySlots.size() - 1; i++) {
                TimeSlot current = daySlots.get(i);
                TimeSlot next = daySlots.get(i + 1);

                long gapMinutes = java.time.Duration.between(
                    current.getEndTime(), next.getStartTime()).toMinutes();

                if (gapMinutes > 60) {
                    penalty += (gapMinutes - 60) * 0.5 * 10.0; // Weight
                }
            }
        }

        return penalty;
    }

    /**
     * Run room allocation after time scheduling
     */
    private void runRoomAllocation() {
        // Update courses with their scheduled times for room allocation
        List<Course> courses = new ArrayList<>();
        for (ScheduledCourse sc : schedule.getScheduledCourses()) {
            if (sc.isScheduled()) {
                courses.add(sc.getCourse());
            }
        }

        if (courses.isEmpty()) {
            System.out.println("No courses scheduled, skipping room allocation.");
            return;
        }

        // Run the type-based allocation algorithm
        Map<String, String> assignments = getAllocator().allocate();

        // Update scheduled courses with room assignments
        for (ScheduledCourse sc : schedule.getScheduledCourses()) {
            String roomId = assignments.get(sc.getCourse().getName());
            if (roomId != null) {
                sc.assignRoom(roomId);
            }
        }

        System.out.println(String.format("Room Allocation Complete: %d/%d courses assigned rooms",
            assignments.size(), courses.size()));
    }
}
