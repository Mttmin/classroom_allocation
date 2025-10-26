package com.roomallocation.allocation;

import com.roomallocation.model.Room;
import com.roomallocation.model.Course;

public class AllocationStep {
    private String description;
    private Course course;
    private Room room;
    private Course displacedCourse;

    public AllocationStep(String description, Course course, Room room, Course displacedCourse) {
        this.description = description;
        this.course = course;
        this.room = room;
        this.displacedCourse = displacedCourse;
    }

    @Override
    public String toString() {
        return description;
    }

    public Course getCourse() {
        return course;
    }
    public Room getRoom() {
        return room;
    }
    public Course getDisplacedCourse() {
        return displacedCourse;
    }
}
