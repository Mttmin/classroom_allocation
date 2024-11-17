package com.roomallocation.model;
// import com.roomallocation.model.Course;

public class Room {
    private String name;
    private int capacity;
    private RoomType type;
    private Course currentOccupant;

    public Room(String name, int capacity, RoomType type) {
        this.name = name;
        this.capacity = capacity;
        this.type = type;
        this.currentOccupant = null;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public RoomType getType() {
        return type;
    }

    public Course getCurrentOccupant() {
        return currentOccupant;
    }

    public void setCurrentOccupant(Course course) {
        this.currentOccupant = course;
        if (course != null) {
            course.setAssignedRoom(name);
        }
    }

    @Override
    public String toString() {
        return name + " (Capacity: " + capacity + ", Type: " + type.getDisplayName() + ")";

    }
}
