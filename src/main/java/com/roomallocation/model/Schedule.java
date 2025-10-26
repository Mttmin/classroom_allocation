package com.roomallocation.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the complete schedule for all courses
 */
public class Schedule {
    private List<ScheduledCourse> scheduledCourses;
    private Map<String, Professor> professors;
    private double[][] correlationMatrix;
    private Map<String, Integer> courseIndexMap;
    private double score;

    public Schedule() {
        this.scheduledCourses = new ArrayList<>();
        this.professors = new HashMap<>();
        this.courseIndexMap = new HashMap<>();
        this.score = 0.0;
    }

    public Schedule(List<Course> courses, Map<String, Professor> professors, double[][] correlationMatrix) {
        this.scheduledCourses = courses.stream()
                                      .map(ScheduledCourse::new)
                                      .collect(Collectors.toList());
        this.professors = new HashMap<>(professors);
        this.correlationMatrix = correlationMatrix;
        this.courseIndexMap = new HashMap<>();

        // Build course index map for correlation matrix lookup
        for (int i = 0; i < courses.size(); i++) {
            courseIndexMap.put(courses.get(i).getName(), i);
        }

        this.score = 0.0;
    }

    /**
     * Add a scheduled course to the schedule
     */
    public void addScheduledCourse(ScheduledCourse scheduledCourse) {
        scheduledCourses.add(scheduledCourse);
        int index = scheduledCourses.size() - 1;
        courseIndexMap.put(scheduledCourse.getCourse().getName(), index);
    }

    /**
     * Get correlation between two courses
     */
    public double getCorrelation(Course course1, Course course2) {
        Integer index1 = courseIndexMap.get(course1.getName());
        Integer index2 = courseIndexMap.get(course2.getName());

        if (index1 == null || index2 == null || correlationMatrix == null) {
            return 0.0;
        }

        return correlationMatrix[index1][index2];
    }

    /**
     * Get correlation by course names
     */
    public double getCorrelation(String courseName1, String courseName2) {
        Integer index1 = courseIndexMap.get(courseName1);
        Integer index2 = courseIndexMap.get(courseName2);

        if (index1 == null || index2 == null || correlationMatrix == null) {
            return 0.0;
        }

        return correlationMatrix[index1][index2];
    }

    /**
     * Get professor for a course
     */
    public Professor getProfessorForCourse(Course course) {
        if (course.getProfessorId() == null) {
            return null;
        }
        return professors.get(course.getProfessorId());
    }

    /**
     * Get all courses taught by a professor
     */
    public List<ScheduledCourse> getCoursesByProfessor(String professorId) {
        return scheduledCourses.stream()
                              .filter(sc -> professorId.equals(sc.getCourse().getProfessorId()))
                              .collect(Collectors.toList());
    }

    /**
     * Get scheduled course by course name
     */
    public ScheduledCourse getScheduledCourse(String courseName) {
        Integer index = courseIndexMap.get(courseName);
        if (index == null || index >= scheduledCourses.size()) {
            return null;
        }
        return scheduledCourses.get(index);
    }

    /**
     * Get all scheduled courses
     */
    public List<ScheduledCourse> getScheduledCourses() {
        return new ArrayList<>(scheduledCourses);
    }

    /**
     * Get only courses that have been assigned a time slot
     */
    public List<ScheduledCourse> getAssignedCourses() {
        return scheduledCourses.stream()
                              .filter(ScheduledCourse::isScheduled)
                              .collect(Collectors.toList());
    }

    /**
     * Get courses that haven't been scheduled yet
     */
    public List<ScheduledCourse> getUnscheduledCourses() {
        return scheduledCourses.stream()
                              .filter(sc -> !sc.isScheduled())
                              .collect(Collectors.toList());
    }

    public Map<String, Professor> getProfessors() {
        return new HashMap<>(professors);
    }

    public double[][] getCorrelationMatrix() {
        return correlationMatrix;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Create a deep copy of this schedule
     */
    public Schedule copy() {
        Schedule copy = new Schedule();

        // Copy scheduled courses
        for (ScheduledCourse sc : this.scheduledCourses) {
            ScheduledCourse scCopy = new ScheduledCourse(sc.getCourse(), sc.getSessionPattern());
            if (sc.getAssignedRoomId() != null) {
                scCopy.assignRoom(sc.getAssignedRoomId());
            }
            copy.addScheduledCourse(scCopy);
        }

        copy.professors = new HashMap<>(this.professors);
        copy.correlationMatrix = this.correlationMatrix;
        copy.score = this.score;

        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Schedule (Score: ").append(String.format("%.2f", score)).append(")\n");
        sb.append("Scheduled courses: ").append(getAssignedCourses().size())
          .append("/").append(scheduledCourses.size()).append("\n");

        for (ScheduledCourse sc : scheduledCourses) {
            if (sc.isScheduled()) {
                sb.append("  ").append(sc).append("\n");
            }
        }

        if (!getUnscheduledCourses().isEmpty()) {
            sb.append("Unscheduled courses:\n");
            for (ScheduledCourse sc : getUnscheduledCourses()) {
                sb.append("  ").append(sc.getCourse().getName()).append("\n");
            }
        }

        return sb.toString();
    }
}
