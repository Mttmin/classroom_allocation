package com.roomallocation.statistics;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compares metrics from multiple scheduler and allocator runs
 */
public class MetricsComparator {

    public static class AlgorithmResult {
        private String algorithmName;
        private SchedulerStatistics schedulerStats;
        private AllocationStatistics allocationStats;

        public AlgorithmResult(String algorithmName, SchedulerStatistics schedulerStats, AllocationStatistics allocationStats) {
            this.algorithmName = algorithmName;
            this.schedulerStats = schedulerStats;
            this.allocationStats = allocationStats;
        }

        public String getAlgorithmName() { return algorithmName; }
        public SchedulerStatistics getSchedulerStats() { return schedulerStats; }
        public AllocationStatistics getAllocationStats() { return allocationStats; }
    }

    private List<AlgorithmResult> results;

    public MetricsComparator() {
        this.results = new ArrayList<>();
    }

    public void addResult(String algorithmName, SchedulerStatistics schedulerStats, AllocationStatistics allocationStats) {
        results.add(new AlgorithmResult(algorithmName, schedulerStats, allocationStats));
    }

    /**
     * Generate a comprehensive comparison report
     */
    public String generateComparisonReport() {
        if (results.isEmpty()) {
            return "No results to compare";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║       ALGORITHM COMPARISON REPORT                          ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

        // Summary comparison table
        sb.append(generateSummaryTable());
        sb.append("\n\n");

        // Scheduling metrics comparison
        sb.append(generateSchedulingMetricsTable());
        sb.append("\n\n");

        // Allocation metrics comparison
        sb.append(generateAllocationMetricsTable());
        sb.append("\n\n");

        // Score breakdown comparison
        sb.append(generateScoreBreakdownTable());
        sb.append("\n\n");

        // Performance comparison
        sb.append(generatePerformanceTable());
        sb.append("\n\n");

        // Winner analysis
        sb.append(generateWinnerAnalysis());

        return sb.toString();
    }

    private String generateSummaryTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("SUMMARY COMPARISON\n");
        sb.append("─────────────────────────────────────────────────────────────\n");

        // Header
        sb.append(String.format("%-30s", "Metric"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", truncate(result.getAlgorithmName(), 18)));
        }
        sb.append("\n");
        sb.append("─────────────────────────────────────────────────────────────\n");

        // Scheduling rate
        sb.append(String.format("%-30s", "Scheduling Rate"));
        for (AlgorithmResult result : results) {
            double rate = result.getSchedulerStats().getSchedulingRate() * 100;
            sb.append(String.format("%-20s", String.format("%.2f%%", rate)));
        }
        sb.append("\n");

        // Allocation rate
        sb.append(String.format("%-30s", "Allocation Rate"));
        for (AlgorithmResult result : results) {
            if (result.getAllocationStats() != null) {
                double rate = result.getAllocationStats().getAllocationRate() * 100;
                sb.append(String.format("%-20s", String.format("%.2f%%", rate)));
            } else {
                sb.append(String.format("%-20s", "N/A"));
            }
        }
        sb.append("\n");

        // Total score
        sb.append(String.format("%-30s", "Total Score (lower=better)"));
        for (AlgorithmResult result : results) {
            double score = result.getSchedulerStats().getTotalScore();
            sb.append(String.format("%-20s", String.format("%.2f", score)));
        }
        sb.append("\n");

        // Execution time
        sb.append(String.format("%-30s", "Execution Time (ms)"));
        for (AlgorithmResult result : results) {
            long time = result.getSchedulerStats().getExecutionTimeMs();
            sb.append(String.format("%-20s", time));
        }
        sb.append("\n");

        return sb.toString();
    }

    private String generateSchedulingMetricsTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("SCHEDULING METRICS\n");
        sb.append("─────────────────────────────────────────────────────────────\n");

        // Header
        sb.append(String.format("%-30s", "Metric"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", truncate(result.getAlgorithmName(), 18)));
        }
        sb.append("\n");
        sb.append("─────────────────────────────────────────────────────────────\n");

        // Courses scheduled
        sb.append(String.format("%-30s", "Courses Scheduled"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s",
                String.format("%d/%d", result.getSchedulerStats().getScheduledCourses(),
                    result.getSchedulerStats().getTotalCourses())));
        }
        sb.append("\n");

        // Average gap
        sb.append(String.format("%-30s", "Avg Professor Gap (min)"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", String.format("%.2f", result.getSchedulerStats().getAverageGapMinutes())));
        }
        sb.append("\n");

        // Max gap
        sb.append(String.format("%-30s", "Max Professor Gap (min)"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", String.format("%.2f", result.getSchedulerStats().getMaxGapMinutes())));
        }
        sb.append("\n");

        // Teaching hours
        sb.append(String.format("%-30s", "Avg Teaching Hours/Prof"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", String.format("%.2f", result.getSchedulerStats().getAverageTeachingHoursPerProfessor())));
        }
        sb.append("\n");

        // Early morning courses
        sb.append(String.format("%-30s", "Early Morning Courses"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", result.getSchedulerStats().getEarlyMorningCourses()));
        }
        sb.append("\n");

        // Average start time
        sb.append(String.format("%-30s", "Average Start Time"));
        for (AlgorithmResult result : results) {
            double startTime = result.getSchedulerStats().getAverageStartTime();
            int hour = (int) Math.floor(startTime);
            int minute = (int) ((startTime - hour) * 60);
            sb.append(String.format("%-20s", String.format("%02d:%02d", hour, minute)));
        }
        sb.append("\n");

        // Correlation conflicts
        sb.append(String.format("%-30s", "Correlation Conflicts"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", result.getSchedulerStats().getCorrelationConflicts()));
        }
        sb.append("\n");

        // Business hours courses (9am-5pm)
        sb.append(String.format("%-30s", "Business Hours (9am-5pm)"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", result.getSchedulerStats().getCoursesInBusinessHours()));
        }
        sb.append("\n");

        // Early morning courses (8am-10am)
        sb.append(String.format("%-30s", "Early Morning (8am-10am)"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", result.getSchedulerStats().getCoursesEarlyMorning()));
        }
        sb.append("\n");

        return sb.toString();
    }

    private String generateAllocationMetricsTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALLOCATION METRICS\n");
        sb.append("─────────────────────────────────────────────────────────────\n");

        // Header
        sb.append(String.format("%-30s", "Metric"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", truncate(result.getAlgorithmName(), 18)));
        }
        sb.append("\n");
        sb.append("─────────────────────────────────────────────────────────────\n");

        // Check if any result has allocation stats
        boolean hasAllocationStats = results.stream()
            .anyMatch(r -> r.getAllocationStats() != null);

        if (!hasAllocationStats) {
            sb.append("No allocation statistics available\n");
            return sb.toString();
        }

        // Allocated courses
        sb.append(String.format("%-30s", "Allocated Courses"));
        for (AlgorithmResult result : results) {
            if (result.getAllocationStats() != null) {
                sb.append(String.format("%-20s",
                    String.format("%d/%d", result.getAllocationStats().getAllocatedCourses(),
                        result.getAllocationStats().getTotalCourses())));
            } else {
                sb.append(String.format("%-20s", "N/A"));
            }
        }
        sb.append("\n");

        // First choice rate
        sb.append(String.format("%-30s", "First Choice Rate"));
        for (AlgorithmResult result : results) {
            if (result.getAllocationStats() != null) {
                double rate = result.getAllocationStats().getFirstChoiceRate() * 100;
                sb.append(String.format("%-20s", String.format("%.2f%%", rate)));
            } else {
                sb.append(String.format("%-20s", "N/A"));
            }
        }
        sb.append("\n");

        // Average choice
        sb.append(String.format("%-30s", "Average Choice Number"));
        for (AlgorithmResult result : results) {
            if (result.getAllocationStats() != null) {
                sb.append(String.format("%-20s", String.format("%.2f", result.getAllocationStats().getAverageChoiceNumber())));
            } else {
                sb.append(String.format("%-20s", "N/A"));
            }
        }
        sb.append("\n");

        // Allocation steps
        sb.append(String.format("%-30s", "Allocation Steps"));
        for (AlgorithmResult result : results) {
            if (result.getAllocationStats() != null) {
                sb.append(String.format("%-20s", result.getAllocationStats().getNumberOfSteps()));
            } else {
                sb.append(String.format("%-20s", "N/A"));
            }
        }
        sb.append("\n");

        return sb.toString();
    }

    private String generateScoreBreakdownTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("SCORE BREAKDOWN (lower is better)\n");
        sb.append("─────────────────────────────────────────────────────────────\n");

        // Collect all score component names
        Set<String> allComponents = new LinkedHashSet<>();
        for (AlgorithmResult result : results) {
            allComponents.addAll(result.getSchedulerStats().getScoreBreakdown().keySet());
        }

        // Header
        sb.append(String.format("%-30s", "Component"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", truncate(result.getAlgorithmName(), 18)));
        }
        sb.append("\n");
        sb.append("─────────────────────────────────────────────────────────────\n");

        // Each component
        for (String component : allComponents) {
            sb.append(String.format("%-30s", truncate(component, 28)));
            for (AlgorithmResult result : results) {
                Double score = result.getSchedulerStats().getScoreBreakdown().get(component);
                if (score != null) {
                    sb.append(String.format("%-20s", String.format("%.2f", score)));
                } else {
                    sb.append(String.format("%-20s", "N/A"));
                }
            }
            sb.append("\n");
        }

        // Total
        sb.append(String.format("%-30s", "TOTAL"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", String.format("%.2f", result.getSchedulerStats().getTotalScore())));
        }
        sb.append("\n");

        return sb.toString();
    }

    private String generatePerformanceTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("PERFORMANCE COMPARISON\n");
        sb.append("─────────────────────────────────────────────────────────────\n");

        // Header
        sb.append(String.format("%-30s", "Metric"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", truncate(result.getAlgorithmName(), 18)));
        }
        sb.append("\n");
        sb.append("─────────────────────────────────────────────────────────────\n");

        // Execution time
        sb.append(String.format("%-30s", "Execution Time (ms)"));
        for (AlgorithmResult result : results) {
            sb.append(String.format("%-20s", result.getSchedulerStats().getExecutionTimeMs()));
        }
        sb.append("\n");

        // Courses per second
        sb.append(String.format("%-30s", "Courses/Second"));
        for (AlgorithmResult result : results) {
            long timeMs = result.getSchedulerStats().getExecutionTimeMs();
            int courses = result.getSchedulerStats().getTotalCourses();
            if (timeMs > 0) {
                double coursesPerSec = (courses * 1000.0) / timeMs;
                sb.append(String.format("%-20s", String.format("%.2f", coursesPerSec)));
            } else {
                sb.append(String.format("%-20s", "N/A"));
            }
        }
        sb.append("\n");

        return sb.toString();
    }

    private String generateWinnerAnalysis() {
        if (results.size() < 2) {
            return "WINNER ANALYSIS\n" +
                   "─────────────────────────────────────────────────────────────\n" +
                   "Need at least 2 algorithms to compare\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("WINNER ANALYSIS\n");
        sb.append("─────────────────────────────────────────────────────────────\n");

        // Find best in each category
        AlgorithmResult bestScore = results.stream()
            .min(Comparator.comparingDouble(r -> r.getSchedulerStats().getTotalScore()))
            .orElse(null);

        AlgorithmResult bestSchedulingRate = results.stream()
            .max(Comparator.comparingDouble(r -> r.getSchedulerStats().getSchedulingRate()))
            .orElse(null);

        AlgorithmResult bestPerformance = results.stream()
            .min(Comparator.comparingLong(r -> r.getSchedulerStats().getExecutionTimeMs()))
            .orElse(null);

        AlgorithmResult bestGaps = results.stream()
            .min(Comparator.comparingDouble(r -> r.getSchedulerStats().getAverageGapMinutes()))
            .orElse(null);

        if (bestScore != null) {
            sb.append(String.format("🏆 Best Schedule Quality: %s (score: %.2f)\n",
                bestScore.getAlgorithmName(), bestScore.getSchedulerStats().getTotalScore()));
        }

        if (bestSchedulingRate != null) {
            sb.append(String.format("🏆 Best Scheduling Rate: %s (%.2f%%)\n",
                bestSchedulingRate.getAlgorithmName(),
                bestSchedulingRate.getSchedulerStats().getSchedulingRate() * 100));
        }

        if (bestPerformance != null) {
            sb.append(String.format("🏆 Fastest Execution: %s (%d ms)\n",
                bestPerformance.getAlgorithmName(),
                bestPerformance.getSchedulerStats().getExecutionTimeMs()));
        }

        if (bestGaps != null) {
            sb.append(String.format("🏆 Best Professor Schedule: %s (avg gap: %.2f min)\n",
                bestGaps.getAlgorithmName(),
                bestGaps.getSchedulerStats().getAverageGapMinutes()));
        }

        // Overall winner (weighted)
        sb.append("\n");
        sb.append("OVERALL RECOMMENDATION\n");
        sb.append("Based on score quality and scheduling rate:\n");

        AlgorithmResult overall = results.stream()
            .min(Comparator.comparingDouble(r -> {
                double normalizedScore = r.getSchedulerStats().getTotalScore() / 1000.0;
                double schedulingPenalty = (1.0 - r.getSchedulerStats().getSchedulingRate()) * 1000;
                return normalizedScore + schedulingPenalty;
            }))
            .orElse(null);

        if (overall != null) {
            sb.append(String.format(">>> %s <<<\n", overall.getAlgorithmName().toUpperCase()));
        }

        return sb.toString();
    }

    /**
     * Export comparison data to a map for JSON serialization
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();

        List<Map<String, Object>> algorithmData = new ArrayList<>();
        for (AlgorithmResult result : results) {
            Map<String, Object> algMap = new LinkedHashMap<>();
            algMap.put("algorithmName", result.getAlgorithmName());
            algMap.put("schedulerStats", result.getSchedulerStats().toMap());
            if (result.getAllocationStats() != null) {
                algMap.put("allocationStats", result.getAllocationStats().toMap());
            }
            algorithmData.add(algMap);
        }

        map.put("algorithms", algorithmData);
        map.put("comparisonReport", generateComparisonReport());

        return map;
    }

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 2) + "..";
    }

    public List<AlgorithmResult> getResults() {
        return results;
    }
}
