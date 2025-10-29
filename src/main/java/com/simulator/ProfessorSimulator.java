package com.simulator;

import com.roomallocation.model.Professor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProfessorSimulator {
    private final Random random;

    public enum AvailabilityMode {
        FULL_TIME,           // Available 100% of standard hours (Mon-Fri 8AM-8PM)
        MOSTLY_AVAILABLE_95, // Available 95% of standard hours (random gaps)
        MOSTLY_AVAILABLE_85, // Available 85% of standard hours (random gaps)
        PART_TIME_50,        // Available ~50% of standard hours
        PART_TIME_30,        // Available ~30% of standard hours
        MORNING_ONLY,        // Available only 8AM-1PM
        AFTERNOON_ONLY,      // Available only 1PM-8PM
        THREE_DAYS_WEEK,     // Available Mon/Wed/Fri only
        TWO_DAYS_WEEK,       // Available Tue/Thu only
        CUSTOM              // Will be set manually
    }

    public ProfessorSimulator() {
        this.random = new Random();
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    /**
     * Generate a list of professors with specified availability modes
     */
    public List<Professor> generateProfessors(int numProfessors, AvailabilityMode mode) {
        List<Professor> professors = new ArrayList<>();
        for (int i = 0; i < numProfessors; i++) {
            String id = "PROF" + String.format("%03d", i + 1);
            String name = generateProfessorName();
            Professor prof = new Professor(id, name);
            setAvailabilityByMode(prof, mode);
            professors.add(prof);
        }
        return professors;
    }

    /**
     * Generate professors with mixed availability modes
     */
    public List<Professor> generateProfessorsWithMixedAvailability(
            int numProfessors,
            double fullTimePct,
            double mostlyAvailablePct,
            double partTimePct) {

        List<Professor> professors = new ArrayList<>();

        for (int i = 0; i < numProfessors; i++) {
            String id = "PROF" + String.format("%03d", i + 1);
            String name = generateProfessorName();
            Professor prof = new Professor(id, name);

            double rand = random.nextDouble();
            if (rand < fullTimePct) {
                setAvailabilityByMode(prof, AvailabilityMode.FULL_TIME);
            } else if (rand < fullTimePct + mostlyAvailablePct) {
                // Randomly choose between 95% or 85% availability
                AvailabilityMode mode = random.nextBoolean() ?
                    AvailabilityMode.MOSTLY_AVAILABLE_95 : AvailabilityMode.MOSTLY_AVAILABLE_85;
                setAvailabilityByMode(prof, mode);
            } else {
                // Randomly choose a part-time mode
                AvailabilityMode[] partTimeModes = {
                    AvailabilityMode.PART_TIME_50,
                    AvailabilityMode.PART_TIME_30,
                    AvailabilityMode.MORNING_ONLY,
                    AvailabilityMode.AFTERNOON_ONLY,
                    AvailabilityMode.THREE_DAYS_WEEK,
                    AvailabilityMode.TWO_DAYS_WEEK
                };
                AvailabilityMode mode = partTimeModes[random.nextInt(partTimeModes.length)];
                setAvailabilityByMode(prof, mode);
            }

            professors.add(prof);
        }

        return professors;
    }

    /**
     * Set professor availability based on the specified mode
     */
    private void setAvailabilityByMode(Professor prof, AvailabilityMode mode) {
        // Standard working hours
        LocalTime standardStart = LocalTime.of(8, 0);
        LocalTime standardEnd = LocalTime.of(20, 0);
        DayOfWeek[] workDays = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};

        switch (mode) {
            case FULL_TIME:
                // Available all standard hours
                for (DayOfWeek day : workDays) {
                    prof.addAvailabilityPeriod(day, standardStart, standardEnd);
                }
                break;

            case MOSTLY_AVAILABLE_95:
                // Remove 5% of time slots randomly
                addAvailabilityWithGaps(prof, workDays, standardStart, standardEnd, 0.05);
                break;

            case MOSTLY_AVAILABLE_85:
                // Remove 15% of time slots randomly
                addAvailabilityWithGaps(prof, workDays, standardStart, standardEnd, 0.15);
                break;

            case PART_TIME_50:
                // Available ~50% of the time (random days and times)
                addPartTimeAvailability(prof, workDays, standardStart, standardEnd, 0.50);
                break;

            case PART_TIME_30:
                // Available ~30% of the time (random days and times)
                addPartTimeAvailability(prof, workDays, standardStart, standardEnd, 0.30);
                break;

            case MORNING_ONLY:
                // Available only mornings (8AM-1PM)
                for (DayOfWeek day : workDays) {
                    prof.addAvailabilityPeriod(day, LocalTime.of(8, 0), LocalTime.of(13, 0));
                }
                break;

            case AFTERNOON_ONLY:
                // Available only afternoons (1PM-8PM)
                for (DayOfWeek day : workDays) {
                    prof.addAvailabilityPeriod(day, LocalTime.of(13, 0), LocalTime.of(20, 0));
                }
                break;

            case THREE_DAYS_WEEK:
                // Available Mon/Wed/Fri only
                prof.addAvailabilityPeriod(DayOfWeek.MONDAY, standardStart, standardEnd);
                prof.addAvailabilityPeriod(DayOfWeek.WEDNESDAY, standardStart, standardEnd);
                prof.addAvailabilityPeriod(DayOfWeek.FRIDAY, standardStart, standardEnd);
                break;

            case TWO_DAYS_WEEK:
                // Available Tue/Thu only
                prof.addAvailabilityPeriod(DayOfWeek.TUESDAY, standardStart, standardEnd);
                prof.addAvailabilityPeriod(DayOfWeek.THURSDAY, standardStart, standardEnd);
                break;

            case CUSTOM:
                // Leave empty for manual configuration
                break;
        }
    }

    /**
     * Add availability with random gaps
     */
    private void addAvailabilityWithGaps(Professor prof, DayOfWeek[] days,
                                        LocalTime startTime, LocalTime endTime,
                                        double gapPercentage) {
        for (DayOfWeek day : days) {
            List<TimeBlock> blocks = generateTimeBlocksWithGaps(startTime, endTime, gapPercentage);
            for (TimeBlock block : blocks) {
                prof.addAvailabilityPeriod(day, block.start, block.end);
            }
        }
    }

    /**
     * Add part-time availability (random subset of days and times)
     */
    private void addPartTimeAvailability(Professor prof, DayOfWeek[] days,
                                        LocalTime startTime, LocalTime endTime,
                                        double availabilityPercentage) {
        // Randomly select which days to be available
        for (DayOfWeek day : days) {
            if (random.nextDouble() < availabilityPercentage * 1.5) { // Adjust to hit target
                // Add a random time block on this day
                int totalMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
                int blockMinutes = (int) (totalMinutes * (0.4 + random.nextDouble() * 0.4)); // 40-80% of day
                int startOffset = random.nextInt(totalMinutes - blockMinutes);

                LocalTime blockStart = startTime.plusMinutes(startOffset);
                LocalTime blockEnd = blockStart.plusMinutes(blockMinutes);

                prof.addAvailabilityPeriod(day, blockStart, blockEnd);
            }
        }
    }

    /**
     * Generate time blocks with random gaps
     */
    private List<TimeBlock> generateTimeBlocksWithGaps(LocalTime start, LocalTime end, double gapPercentage) {
        List<TimeBlock> blocks = new ArrayList<>();
        int totalMinutes = (int) java.time.Duration.between(start, end).toMinutes();
        int gapMinutes = (int) (totalMinutes * gapPercentage);

        if (gapMinutes == 0 || random.nextDouble() > gapPercentage * 10) {
            // No gap, return full block
            blocks.add(new TimeBlock(start, end));
            return blocks;
        }

        // Create 1-3 random gaps
        int numGaps = 1 + random.nextInt(Math.min(3, (gapMinutes / 30) + 1));
        int gapSizePerGap = gapMinutes / numGaps;

        LocalTime currentTime = start;
        for (int i = 0; i < numGaps && currentTime.isBefore(end); i++) {
            // Available block duration
            int availableMinutes = 60 + random.nextInt(180); // 1-4 hours
            LocalTime blockEnd = currentTime.plusMinutes(availableMinutes);

            if (blockEnd.isAfter(end)) {
                blockEnd = end;
            }

            blocks.add(new TimeBlock(currentTime, blockEnd));

            // Add gap
            currentTime = blockEnd.plusMinutes(gapSizePerGap);
        }

        // Add final block if there's time remaining
        if (currentTime.isBefore(end)) {
            blocks.add(new TimeBlock(currentTime, end));
        }

        return blocks;
    }

    /**
     * Generate a random professor name
     */
    private String generateProfessorName() {
        String[] firstNames = {
            "Jean", "Marie", "Pierre", "Sophie", "Luc", "Claire", "Michel", "Anne",
            "Thomas", "Julie", "François", "Isabelle", "Nicolas", "Catherine", "Philippe",
            "Nathalie", "Laurent", "Sylvie", "David", "Martine", "Alain", "Christine",
            "Patrick", "Monique", "Bernard", "Françoise", "Eric", "Brigitte", "Olivier"
        };

        String[] lastNames = {
            "Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard", "Petit", "Durand",
            "Leroy", "Moreau", "Simon", "Laurent", "Lefebvre", "Michel", "Garcia", "David",
            "Bertrand", "Roux", "Vincent", "Fournier", "Morel", "Girard", "Andre", "Mercier",
            "Dupont", "Lambert", "Bonnet", "François", "Martinez"
        };

        return firstNames[random.nextInt(firstNames.length)] + " " +
               lastNames[random.nextInt(lastNames.length)];
    }

    /**
     * Helper class to represent a time block
     */
    private static class TimeBlock {
        final LocalTime start;
        final LocalTime end;

        TimeBlock(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
