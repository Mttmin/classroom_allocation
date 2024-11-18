package com.roomallocation.fitmethods;

import com.roomallocation.model.Course;
import com.roomallocation.model.Room;

public class capaFit {
    public static double capafit(Room room, Course course) {
        return Math.abs(room.getCapacity() - course.getCohortSize());
    }
}
