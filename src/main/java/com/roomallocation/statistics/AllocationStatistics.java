package com.roomallocation.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.roomallocation.allocation.AllocationStep;
import com.roomallocation.model.Course;

public class AllocationStatistics {
    private int totalCourses;
    private int allocatedCourses;
    private Map<Integer, Integer> choiceDistribution;
    private double averageChoice;
    private double allocationRate;
    private double firstChoiceRate;
    private double highRankRate;
    private double unallocatedRate;
    private int numAllocationSteps;
    private String strategyName;

    public AllocationStatistics(String strategyName, List<Course> courses, List<AllocationStep> steps) {
        this.strategyName = strategyName;
        this.totalCourses = courses.size();
        this.choiceDistribution = new HashMap<>();
        this.numAllocationSteps = steps.size();
        calculateStatistics(courses);
    }

    private void calculateStatistics(List<Course> courses) {
        allocatedCourses = 0;
        int totalChoiceNumber = 0;
        int firstChoices = 0;
        int highRankChoices = 0;
        
        for (Course course : courses) {
            if (course.getAssignedRoom() != null) {
                allocatedCourses++;
                int choiceNumber = course.getChoiceNumber();
                choiceDistribution.merge(choiceNumber, 1, Integer::sum);
                totalChoiceNumber += choiceNumber;
                
                if (choiceNumber == 1) {
                    firstChoices++;
                }
                if (choiceNumber >= 4) {
                    highRankChoices++;
                }
            }
        }
        
        // Calculate rates
        averageChoice = allocatedCourses > 0 ? (double) totalChoiceNumber / allocatedCourses : 0;
        allocationRate = (double) allocatedCourses / totalCourses * 100;
        firstChoiceRate = allocatedCourses > 0 ? (double) firstChoices / totalCourses * 100 : 0;
        highRankRate = allocatedCourses > 0 ? (double) highRankChoices / totalCourses * 100 : 0;
        unallocatedRate = (double) (totalCourses - allocatedCourses) / totalCourses * 100;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("strategyName", strategyName);
        stats.put("totalCourses", totalCourses);
        stats.put("allocatedCourses", allocatedCourses);
        stats.put("unallocatedCourses", totalCourses - allocatedCourses);
        stats.put("choiceDistribution", choiceDistribution);
        stats.put("averageChoice", String.format("%.2f", averageChoice));
        stats.put("allocationRate", String.format("%.1f", allocationRate));
        stats.put("firstChoiceRate", String.format("%.1f", firstChoiceRate));
        stats.put("highRankRate", String.format("%.1f", highRankRate));
        stats.put("unallocatedRate", String.format("%.1f", unallocatedRate));
        stats.put("numAllocationSteps", numAllocationSteps);
        return stats;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Strategy: ").append(strategyName).append("\n");
        sb.append("Total Courses: ").append(totalCourses).append("\n");
        sb.append("Allocated Courses: ").append(allocatedCourses).append("\n");
        sb.append("Unallocated Courses: ").append(totalCourses - allocatedCourses)
          .append(" (").append(String.format("%.1f%%", unallocatedRate)).append(")\n");
        
        sb.append("\nAllocation Quality Metrics:\n");
        sb.append("First Choice Rate: ").append(String.format("%.1f%%", firstChoiceRate)).append("\n");
        sb.append("High Rank Rate (4+): ").append(String.format("%.1f%%", highRankRate)).append("\n");
        sb.append("Average Choice: ").append(String.format("%.2f", averageChoice)).append("\n");
        sb.append("Overall Satisfaction Rate: ").append(String.format("%.1f%%", allocationRate)).append("\n");
        
        sb.append("\nChoice Distribution:\n");
        choiceDistribution.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> 
                sb.append("  Choice #").append(entry.getKey())
                  .append(": ").append(entry.getValue())
                  .append(" (").append(String.format("%.1f%%", (double)entry.getValue() / totalCourses * 100))
                  .append(")\n")
            );
        
        sb.append("\nAllocation Process:\n");
        sb.append("Number of Allocation Steps: ").append(numAllocationSteps).append("\n");
        
        return sb.toString();
    }

    // Getters for the metrics
    public double getSatisfactionRate() { return allocationRate; }
    public double getAllocationRate() { return allocationRate / 100.0; } // Return as decimal (0-1)
    public double getFirstChoiceRate() { return firstChoiceRate / 100.0; } // Return as decimal (0-1)
    public double getHighRankRate() { return highRankRate; }
    public double getUnallocatedRate() { return unallocatedRate; }
    public double getAverageChoice() { return averageChoice; }
    public double getAverageChoiceNumber() { return averageChoice; } // Alias
    public int getNumAllocationSteps() { return numAllocationSteps; }
    public int getNumberOfSteps() { return numAllocationSteps; } // Alias
    public int getTotalCourses() { return totalCourses; }
    public int getAllocatedCourses() { return allocatedCourses; }
    public Map<Integer, Integer> getChoiceDistribution() { return choiceDistribution; }
}

