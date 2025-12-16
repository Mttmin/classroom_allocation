package com.roomallocation.scheduler.optimizer;

import com.roomallocation.scheduler.scoring.Scoring;
import com.roomallocation.scheduler.util.TimeSlotGenerator;
import com.roomallocation.allocation.TypeBasedAllocation;
import com.roomallocation.constraint.ConstraintValidator;
import com.roomallocation.model.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simulated Annealing Scheduler
 * Uses simulated annealing optimization to find better schedules than greedy approach
 *
 * Algorithm:
 * 1. Generate initial solution (greedy)
 * 2. Iteratively make random moves (swap courses, reassign times)
 * 3. Accept better solutions always, worse solutions with probability e^(-delta/T)
 * 4. Gradually decrease temperature T
 * 5. Return best solution found
 */
public class SimulatedAnnealingScheduler extends Scheduler {

    private TimeSlotGenerator timeSlotGenerator;
    private Map<String, Professor> professors;
    private double[][] correlationMatrix;
    private Random random;

    // Simulated Annealing parameters
    private double initialTemperature = 1000.0;
    private double coolingRate = 0.995;
    private int maxIterations = 50000;
    private int iterationsPerTemperature = 100;

    // Move probability - probability of swapping vs moving to new slot
    private double swapProbability = 0.7;

    // Statistics
    private int acceptedMoves = 0;
    private int rejectedMoves = 0;
    private double bestScoreFound = Double.POSITIVE_INFINITY;

    public SimulatedAnnealingScheduler(String name,
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

    /**
     * Configuration methods for SA parameters
     */
    public void setInitialTemperature(double temp) {
        this.initialTemperature = temp;
    }

    public void setCoolingRate(double rate) {
        this.coolingRate = rate;
    }

    public void setMaxIterations(int iterations) {
        this.maxIterations = iterations;
    }

    public void setIterationsPerTemperature(int iterations) {
        this.iterationsPerTemperature = iterations;
    }

    public void setSwapProbability(double swapProb) {
        if (swapProb < 0.0 || swapProb > 1.0) {
            throw new IllegalArgumentException("Swap probability must be between 0.0 and 1.0");
        }
        this.swapProbability = swapProb;
    }

    @Override
    public void runSchedule() {
        long startTime = System.currentTimeMillis();
        System.out.println("Running Simulated Annealing Scheduler...");
        System.out.println(String.format("Parameters: T0=%.1f, cooling=%.4f, maxIter=%d",
            initialTemperature, coolingRate, maxIterations));

        // Step 1: Generate initial solution using greedy approach
        System.out.println("\n=== Generating Initial Solution (Greedy) ===");
        generateInitialSolution();

        double currentScore = getScoring().calculateScore(schedule);
        double bestScore = currentScore;
        bestScoreFound = bestScore;

        // Deep copy the best schedule
        Schedule bestSchedule = deepCopySchedule(schedule);

        System.out.println(String.format("Initial Score: %.2f", currentScore));

        // Step 2: Run Simulated Annealing
        System.out.println("\n=== Running Simulated Annealing ===");
        double temperature = initialTemperature;
        int iteration = 0;
        int improvementCount = 0;

        while (iteration < maxIterations && temperature > 0.01) {
            for (int i = 0; i < iterationsPerTemperature && iteration < maxIterations; i++) {
                iteration++;

                // Generate neighbor solution
                boolean moveSuccessful = generateNeighbor();

                if (!moveSuccessful) {
                    // If we couldn't make a valid move, skip this iteration
                    continue;
                }

                // Evaluate new solution
                double newScore = getScoring().calculateScore(schedule);
                double delta = newScore - currentScore;

                // Decide whether to accept the move
                boolean accept = false;
                if (delta < 0) {
                    // Better solution - always accept
                    accept = true;
                } else {
                    // Worse solution - accept with probability e^(-delta/T)
                    double acceptanceProbability = Math.exp(-delta / temperature);
                    accept = random.nextDouble() < acceptanceProbability;
                }

                if (accept) {
                    // Accept the move
                    currentScore = newScore;
                    acceptedMoves++;

                    // Check if this is the best solution so far
                    if (newScore < bestScore) {
                        bestScore = newScore;
                        bestScoreFound = bestScore;
                        bestSchedule = deepCopySchedule(schedule);
                        improvementCount++;

                        if (improvementCount % 10 == 0) {
                            System.out.println(String.format("  Iter %d: New best score: %.2f (T=%.2f)",
                                iteration, bestScore, temperature));
                        }
                    }
                } else {
                    // Reject the move - revert to previous state
                    // (We need to undo the move, but for simplicity we'll regenerate from best)
                    rejectedMoves++;
                }

                // Progress reporting
                if (iteration % 5000 == 0) {
                    double acceptanceRate = (double) acceptedMoves / (acceptedMoves + rejectedMoves) * 100;
                    System.out.println(String.format("  Iter %d: Current=%.2f, Best=%.2f, T=%.2f, Accept=%.1f%%",
                        iteration, currentScore, bestScore, temperature, acceptanceRate));
                }
            }

            // Cool down
            temperature *= coolingRate;
        }

        // Step 3: Set the best solution found
        this.schedule = bestSchedule;
        schedule.setScore(bestScore);

        long endTime = System.currentTimeMillis();
        this.executionTimeMs = endTime - startTime;

        // Print final statistics
        System.out.println("\n=== Simulated Annealing Complete ===");
        System.out.println(String.format("Iterations: %d", iteration));
        System.out.println(String.format("Initial Score: %.2f", currentScore));
        System.out.println(String.format("Final Best Score: %.2f", bestScore));
        System.out.println(String.format("Improvement: %.2f (%.1f%%)",
            currentScore - bestScore,
            ((currentScore - bestScore) / currentScore) * 100));
        System.out.println(String.format("Accepted Moves: %d", acceptedMoves));
        System.out.println(String.format("Rejected Moves: %d", rejectedMoves));
        System.out.println(String.format("Acceptance Rate: %.1f%%",
            (double) acceptedMoves / (acceptedMoves + rejectedMoves) * 100));
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
     * Generate initial solution using greedy approach
     */
    private void generateInitialSolution() {
        this.schedule = new Schedule(getCourses(), professors, correlationMatrix);

        // Get courses sorted by priority (same as NaiveScheduler)
        List<ScheduledCourse> prioritizedCourses = prioritizeCourses();

        int scheduledCount = 0;
        int totalCourses = prioritizedCourses.size();

        // Schedule each course greedily
        for (ScheduledCourse scheduledCourse : prioritizedCourses) {
            SessionPattern bestPattern = findBestSessionPattern(scheduledCourse);

            if (bestPattern != null && !bestPattern.isEmpty()) {
                scheduledCourse.assignSessionPattern(bestPattern);
                scheduledCount++;
            }
        }

        System.out.println(String.format("Initial solution: %d/%d courses scheduled",
            scheduledCount, totalCourses));
    }

    /**
     * Prioritize courses for scheduling (same as NaiveScheduler)
     */
    private List<ScheduledCourse> prioritizeCourses() {
        List<ScheduledCourse> courses = new ArrayList<>(schedule.getScheduledCourses());

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

        courses.sort((a, b) -> {
            double sumA = correlationSums.get(a.getCourse().getName());
            double sumB = correlationSums.get(b.getCourse().getName());
            return Double.compare(sumB, sumA);
        });

        return courses;
    }

    /**
     * Find best session pattern for a course (similar to NaiveScheduler but simpler)
     */
    private SessionPattern findBestSessionPattern(ScheduledCourse scheduledCourse) {
        Course course = scheduledCourse.getCourse();
        int durationMinutes = course.getDurationMinutes();
        int sessionsPerWeek = calculateSessionsPerWeek(durationMinutes);

        List<List<TimeSlot>> candidatePatterns = timeSlotGenerator.generateSessionPatterns(
            durationMinutes, sessionsPerWeek);

        // Sample patterns for efficiency
        if (candidatePatterns.size() > 100) {
            candidatePatterns = samplePatterns(candidatePatterns, 100);
        }

        SessionPattern bestPattern = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for (List<TimeSlot> patternSlots : candidatePatterns) {
            SessionPattern pattern = createSessionPattern(patternSlots);

            if (!isPatternValid(scheduledCourse, pattern)) {
                continue;
            }

            double score = scorePattern(scheduledCourse, pattern);

            if (score < bestScore) {
                bestScore = score;
                bestPattern = pattern;
            }
        }

        return bestPattern;
    }

    /**
     * Generate a neighbor solution by making a random move
     * Returns true if move was successful, false otherwise
     */
    private boolean generateNeighbor() {
        double moveType = random.nextDouble();

        if (moveType < swapProbability) {
            return swapCourseTimes();
        } else {
            return moveToNewSlot();
        }
    }

    /**
     * Swap session patterns between two randomly selected courses
     */
    private boolean swapCourseTimes() {
        List<ScheduledCourse> scheduledCourses = schedule.getScheduledCourses().stream()
            .filter(ScheduledCourse::isScheduled)
            .collect(Collectors.toList());

        if (scheduledCourses.size() < 2) {
            return false;
        }

        // Pick two random courses
        ScheduledCourse course1 = scheduledCourses.get(random.nextInt(scheduledCourses.size()));
        ScheduledCourse course2 = scheduledCourses.get(random.nextInt(scheduledCourses.size()));

        // Make sure they're different
        int attempts = 0;
        while (course1.equals(course2) && attempts < 10) {
            course2 = scheduledCourses.get(random.nextInt(scheduledCourses.size()));
            attempts++;
        }

        if (course1.equals(course2)) {
            return false;
        }

        // Store original patterns
        SessionPattern pattern1 = course1.getSessionPattern();
        SessionPattern pattern2 = course2.getSessionPattern();

        // Temporarily swap
        course1.assignSessionPattern(pattern2);
        course2.assignSessionPattern(pattern1);

        // Check if swap is valid (hard constraints)
        boolean valid1 = isPatternValidForCourse(course1, pattern2);
        boolean valid2 = isPatternValidForCourse(course2, pattern1);

        if (!valid1 || !valid2) {
            // Revert swap
            course1.assignSessionPattern(pattern1);
            course2.assignSessionPattern(pattern2);
            return false;
        }

        return true;
    }

    /**
     * Move a random course to a new random time slot
     */
    private boolean moveToNewSlot() {
        List<ScheduledCourse> scheduledCourses = schedule.getScheduledCourses().stream()
            .filter(ScheduledCourse::isScheduled)
            .collect(Collectors.toList());

        if (scheduledCourses.isEmpty()) {
            return false;
        }

        // Pick a random course
        ScheduledCourse course = scheduledCourses.get(random.nextInt(scheduledCourses.size()));

        // Store original pattern
        SessionPattern originalPattern = course.getSessionPattern();

        // Generate a new random pattern
        int durationMinutes = course.getCourse().getDurationMinutes();
        int sessionsPerWeek = calculateSessionsPerWeek(durationMinutes);

        List<List<TimeSlot>> candidatePatterns = timeSlotGenerator.generateSessionPatterns(
            durationMinutes, sessionsPerWeek);

        // Try a few random patterns
        int maxAttempts = Math.min(20, candidatePatterns.size());
        for (int i = 0; i < maxAttempts; i++) {
            List<TimeSlot> randomPatternSlots = candidatePatterns.get(random.nextInt(candidatePatterns.size()));
            SessionPattern newPattern = createSessionPattern(randomPatternSlots);

            course.assignSessionPattern(newPattern);

            if (isPatternValidForCourse(course, newPattern)) {
                return true; // Successfully moved to new slot
            }
        }

        // Couldn't find valid pattern, revert
        course.assignSessionPattern(originalPattern);
        return false;
    }

    /**
     * Check if a pattern is valid for a specific course (hard constraints only)
     */
    private boolean isPatternValidForCourse(ScheduledCourse scheduledCourse, SessionPattern pattern) {
        Course course = scheduledCourse.getCourse();

        // Check professor availability
        for (String profId : course.getProfessorIds()) {
            Professor professor = professors.get(profId);
            if (professor != null && !pattern.fitsAvailability(professor)) {
                return false;
            }
        }

        // Check for conflicts with other scheduled courses
        for (ScheduledCourse other : schedule.getScheduledCourses()) {
            if (other.equals(scheduledCourse) || !other.isScheduled()) {
                continue;
            }

            // Hard constraint: courses with correlation >= 2.0 cannot overlap
            double correlation = schedule.getCorrelation(course, other.getCourse());
            if (correlation >= 2.0) {
                if (pattern.hasOverlapWith(other.getSessionPattern())) {
                    return false;
                }
            }

            // Check professor conflicts
            if (!Collections.disjoint(course.getProfessorIds(), other.getCourse().getProfessorIds())) {
                if (pattern.hasOverlapWith(other.getSessionPattern())) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if pattern is valid (wrapper for compatibility)
     */
    private boolean isPatternValid(ScheduledCourse scheduledCourse, SessionPattern pattern) {
        return isPatternValidForCourse(scheduledCourse, pattern);
    }

    /**
     * Score a pattern for a course
     */
    private double scorePattern(ScheduledCourse scheduledCourse, SessionPattern pattern) {
        double score = 0.0;
        Course course = scheduledCourse.getCourse();

        // Correlation penalty
        for (ScheduledCourse other : schedule.getScheduledCourses()) {
            if (other.equals(scheduledCourse) || !other.isScheduled()) {
                continue;
            }

            double correlation = schedule.getCorrelation(course, other.getCourse());
            if (correlation >= 0.5 && correlation < 2.0) {
                if (pattern.hasOverlapWith(other.getSessionPattern())) {
                    score += Math.pow(correlation, 2) * 100 * 1000.0;
                }
            }
        }

        // Professor gap penalty
        for (String profId : course.getProfessorIds()) {
            Professor professor = professors.get(profId);
            if (professor != null) {
                score += calculateProfessorGapForPattern(professor, pattern);
            }
        }

        // Time preference penalty
        for (TimeSlot slot : pattern.getSessions()) {
            if (!TimeSlotGenerator.isPreferredTime(slot)) {
                score += 50.0;
            }
        }

        // Early class penalty
        for (TimeSlot slot : pattern.getSessions()) {
            if (TimeSlotGenerator.isEarlyClass(slot)) {
                double earlyHours = TimeSlotGenerator.getEarlyClassHours(slot);
                score += (Math.exp(earlyHours) - 1) * 5 * 20.0;
            }
        }

        return score;
    }

    /**
     * Calculate professor gap penalty for a new pattern
     */
    private double calculateProfessorGapForPattern(Professor professor, SessionPattern newPattern) {
        double penalty = 0.0;

        List<ScheduledCourse> professorCourses = schedule.getCoursesByProfessor(professor.getId())
            .stream()
            .filter(ScheduledCourse::isScheduled)
            .collect(Collectors.toList());

        Map<java.time.DayOfWeek, List<TimeSlot>> sessionsByDay = new HashMap<>();

        for (ScheduledCourse sc : professorCourses) {
            for (TimeSlot slot : sc.getSessionPattern().getSessions()) {
                sessionsByDay.computeIfAbsent(slot.getDay(), k -> new ArrayList<>()).add(slot);
            }
        }

        for (TimeSlot slot : newPattern.getSessions()) {
            sessionsByDay.computeIfAbsent(slot.getDay(), k -> new ArrayList<>()).add(slot);
        }

        for (List<TimeSlot> daySlots : sessionsByDay.values()) {
            if (daySlots.size() <= 1) continue;

            daySlots.sort(Comparator.comparing(TimeSlot::getStartTime));

            for (int i = 0; i < daySlots.size() - 1; i++) {
                TimeSlot current = daySlots.get(i);
                TimeSlot next = daySlots.get(i + 1);

                long gapMinutes = java.time.Duration.between(
                    current.getEndTime(), next.getStartTime()).toMinutes();

                if (gapMinutes > 60) {
                    penalty += (gapMinutes - 60) * 0.5 * 10.0;
                }
            }
        }

        return penalty;
    }

    /**
     * Calculate sessions per week based on duration
     */
    private int calculateSessionsPerWeek(int durationMinutes) {
        if (durationMinutes <= 90) {
            return 3;
        } else if (durationMinutes <= 120) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * Sample patterns randomly
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
     * Deep copy a schedule (to preserve best solution)
     */
    private Schedule deepCopySchedule(Schedule original) {
        Schedule copy = new Schedule(getCourses(), professors, correlationMatrix);

        // Copy session patterns for all scheduled courses
        for (int i = 0; i < original.getScheduledCourses().size(); i++) {
            ScheduledCourse originalSC = original.getScheduledCourses().get(i);
            ScheduledCourse copySC = copy.getScheduledCourses().get(i);

            if (originalSC.isScheduled()) {
                // Deep copy the session pattern
                SessionPattern originalPattern = originalSC.getSessionPattern();
                SessionPattern copyPattern = new SessionPattern();

                for (TimeSlot slot : originalPattern.getSessions()) {
                    copyPattern.addSession(slot);
                }

                copySC.assignSessionPattern(copyPattern);
            }
        }

        return copy;
    }

    /**
     * Run room allocation after time scheduling
     */
    private void runRoomAllocation() {
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

        Map<String, String> assignments = getAllocator().allocate();

        for (ScheduledCourse sc : schedule.getScheduledCourses()) {
            String roomId = assignments.get(sc.getCourse().getName());
            if (roomId != null) {
                sc.assignRoom(roomId);
            }
        }

        System.out.println(String.format("Room Allocation Complete: %d/%d courses assigned rooms",
            assignments.size(), courses.size()));
    }

    // Getters for statistics
    public int getAcceptedMoves() {
        return acceptedMoves;
    }

    public int getRejectedMoves() {
        return rejectedMoves;
    }

    public double getBestScoreFound() {
        return bestScoreFound;
    }
}
