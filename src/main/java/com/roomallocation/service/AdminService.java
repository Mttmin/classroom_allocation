package com.roomallocation.service;

import com.roomallocation.model.Course;
import com.roomallocation.model.Professor;
import com.roomallocation.model.Room;
import com.roomallocation.util.PreferenceCompletionUtil;

import java.util.*;

/**
 * Service for admin-related operations and statistics
 */
public class AdminService {

    /**
     * Get system-wide statistics
     */
    public Map<String, Object> getSystemStatistics(
            List<Professor> professors,
            List<Course> courses,
            List<Room> rooms) {

        Map<String, Object> stats = new HashMap<>();

        // Basic counts
        stats.put("totalProfessors", professors.size());
        stats.put("totalCourses", courses.size());
        stats.put("totalRooms", rooms.size());

        // Professor preference statistics
        Map<String, Object> preferenceStats = getPreferenceStatistics(professors, courses);
        stats.put("preferenceStatistics", preferenceStats);

        // Room statistics by type
        Map<String, Integer> roomsByType = getRoomCountsByType(rooms);
        stats.put("roomsByType", roomsByType);

        // Course statistics
        Map<String, Object> courseStats = getCourseStatistics(courses);
        stats.put("courseStatistics", courseStats);

        return stats;
    }

    /**
     * Get statistics about professor preferences
     */
    public Map<String, Object> getPreferenceStatistics(List<Professor> professors, List<Course> courses) {
        Map<String, Object> stats = new HashMap<>();

        // Group courses by professor
        Map<String, List<Course>> coursesByProfessor = new HashMap<>();
        for (Course course : courses) {
            if (course.getProfessorId() != null) {
                coursesByProfessor
                    .computeIfAbsent(course.getProfessorId(), k -> new ArrayList<>())
                    .add(course);
            }
        }

        int professorsWithPreferences = 0;
        int professorsWithoutPreferences = 0;
        int professorsWithPartialPreferences = 0;
        int professorsWithNoCourses = 0;
        int totalCoursesWithPreferences = 0;
        int totalCoursesWithoutPreferences = 0;

        List<Map<String, Object>> professorDetails = new ArrayList<>();

        for (Professor professor : professors) {
            List<Course> profCourses = coursesByProfessor.getOrDefault(professor.getId(), new ArrayList<>());

            if (profCourses.isEmpty()) {
                professorsWithNoCourses++;
                continue;
            }

            int coursesWithPrefs = 0;
            int coursesWithoutPrefs = 0;

            for (Course course : profCourses) {
                if (PreferenceCompletionUtil.needsPreferenceCompletion(course)) {
                    coursesWithoutPrefs++;
                    totalCoursesWithoutPreferences++;
                } else {
                    coursesWithPrefs++;
                    totalCoursesWithPreferences++;
                }
            }

            Map<String, Object> profDetail = new HashMap<>();
            profDetail.put("professorId", professor.getId());
            profDetail.put("professorName", professor.getName());
            profDetail.put("totalCourses", profCourses.size());
            profDetail.put("coursesWithPreferences", coursesWithPrefs);
            profDetail.put("coursesWithoutPreferences", coursesWithoutPrefs);

            professorDetails.add(profDetail);

            if (coursesWithoutPrefs == 0) {
                professorsWithPreferences++;
            } else if (coursesWithPrefs == 0) {
                professorsWithoutPreferences++;
            } else {
                professorsWithPartialPreferences++;
            }
        }

        stats.put("professorsWithAllPreferences", professorsWithPreferences);
        stats.put("professorsWithNoPreferences", professorsWithoutPreferences);
        stats.put("professorsWithPartialPreferences", professorsWithPartialPreferences);
        stats.put("professorsWithNoCourses", professorsWithNoCourses);
        stats.put("totalCoursesWithPreferences", totalCoursesWithPreferences);
        stats.put("totalCoursesWithoutPreferences", totalCoursesWithoutPreferences);
        stats.put("professorDetails", professorDetails);

        return stats;
    }

    /**
     * Get room counts by type
     */
    private Map<String, Integer> getRoomCountsByType(List<Room> rooms) {
        Map<String, Integer> counts = new HashMap<>();

        for (Room room : rooms) {
            String typeName = room.getType().name();
            counts.put(typeName, counts.getOrDefault(typeName, 0) + 1);
        }

        return counts;
    }

    /**
     * Get course statistics
     */
    private Map<String, Object> getCourseStatistics(List<Course> courses) {
        Map<String, Object> stats = new HashMap<>();

        int assignedCourses = 0;
        int unassignedCourses = 0;
        int totalStudents = 0;
        int minCohortSize = Integer.MAX_VALUE;
        int maxCohortSize = 0;

        for (Course course : courses) {
            if (course.getAssignedRoom() != null) {
                assignedCourses++;
            } else {
                unassignedCourses++;
            }

            totalStudents += course.getCohortSize();
            minCohortSize = Math.min(minCohortSize, course.getCohortSize());
            maxCohortSize = Math.max(maxCohortSize, course.getCohortSize());
        }

        stats.put("assignedCourses", assignedCourses);
        stats.put("unassignedCourses", unassignedCourses);
        stats.put("totalStudents", totalStudents);
        stats.put("averageCohortSize", courses.isEmpty() ? 0 : totalStudents / courses.size());
        stats.put("minCohortSize", minCohortSize == Integer.MAX_VALUE ? 0 : minCohortSize);
        stats.put("maxCohortSize", maxCohortSize);

        return stats;
    }

    /**
     * Get list of professors who haven't entered preferences
     */
    public List<Map<String, Object>> getProfessorsWithoutPreferences(
            List<Professor> professors,
            List<Course> courses) {

        Map<String, List<Course>> coursesByProfessor = new HashMap<>();
        for (Course course : courses) {
            if (course.getProfessorId() != null) {
                coursesByProfessor
                    .computeIfAbsent(course.getProfessorId(), k -> new ArrayList<>())
                    .add(course);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Professor professor : professors) {
            List<Course> profCourses = coursesByProfessor.getOrDefault(professor.getId(), new ArrayList<>());

            if (profCourses.isEmpty()) {
                continue;
            }

            boolean hasCoursesWithoutPrefs = profCourses.stream()
                .anyMatch(PreferenceCompletionUtil::needsPreferenceCompletion);

            if (hasCoursesWithoutPrefs) {
                Map<String, Object> profInfo = new HashMap<>();
                profInfo.put("professorId", professor.getId());
                profInfo.put("professorName", professor.getName());
                profInfo.put("coursesWithoutPreferences",
                    profCourses.stream()
                        .filter(PreferenceCompletionUtil::needsPreferenceCompletion)
                        .map(Course::getName)
                        .toList()
                );
                result.add(profInfo);
            }
        }

        return result;
    }
}
