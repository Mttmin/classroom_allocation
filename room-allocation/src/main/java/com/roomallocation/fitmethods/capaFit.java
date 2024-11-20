package com.roomallocation.fitmethods;

import com.roomallocation.model.Course;
import com.roomallocation.model.Room;

public class capaFit {
    public static double capafit(Room room, Course course) {
        // If room is too small for course, return infinity
        if (room.getCapacity() < course.getCohortSize()) {
            return Double.POSITIVE_INFINITY;
        }
        
        // Otherwise return the amount of wasted space
        return room.getCapacity() - course.getCohortSize();
    }
}
