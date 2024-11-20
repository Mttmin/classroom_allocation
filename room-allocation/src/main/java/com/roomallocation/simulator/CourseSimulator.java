package com.roomallocation.simulator;

import com.roomallocation.model.Course;
import com.roomallocation.strategy.PreferenceGenerationStrategy;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class CourseSimulator {
    private final PreferenceGenerationStrategy strategy;
    private final Random random;

    public void setSeed(long seed) {
        random.setSeed(seed);
    }
    public CourseSimulator(PreferenceGenerationStrategy strategy) {
        this.strategy = strategy;
        this.random = new Random();
    }
    
    private String generateSubject() {
        String[] subjects = {"MAT", "MAP", "HSS", "LAN", "MEC", "ECO", "BIO", "INF", "PHY", "CHI"};
        String matter = subjects[random.nextInt(subjects.length)];
        String number = Integer.toString(300 + random.nextInt(200));
        return matter + number;
    }

    public List<Course> generateCourses(int numCourses, int minSize, int maxSize, int changeSize) {
        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) {
            int size;
            if (random.nextDouble() < 0.9) {
                size = random.nextInt(changeSize - minSize + 1) + minSize;
            } else {
                size = random.nextInt(maxSize - changeSize + 1) + changeSize;
            }
            courses.add(new Course(generateSubject(), size));
            courses.get(i).setTypePreferences(strategy.generatePreferences(courses.get(i)));
        }
        return courses;
    }
}
