package com.roomallocation.scheduler.optimizer;
import com.roomallocation.model.Course;
import com.roomallocation.model.Professor;
import com.roomallocation.model.Room;
import com.roomallocation.scheduler.scoring.Scoring;
import com.roomallocation.allocation.TypeBasedAllocation;
import com.roomallocation.constraint.ConstraintValidator;
import com.roomallocation.model.Schedule;
import java.util.List;
import java.util.Map;


public abstract class Scheduler {
    private String name;
    private Scoring scoring;
    private ConstraintValidator constraints;
    private List<Course> courses;
    private List<Room> rooms;
    private TypeBasedAllocation allocator;
    public Schedule schedule;
    public boolean scheduled = false;
    private boolean forcereassign = false;
    private Map<String, Professor> professors;
    private double[][] correlationMatrix;

    public Scheduler(String name, Scoring scoring, ConstraintValidator constraints, List<Course> courses, List<Room> rooms, TypeBasedAllocation allocator, boolean forcereassign) {
        this.name = name;
        this.scoring = scoring;
        this.constraints = constraints;
        this.courses = courses;
        this.rooms = rooms;
        this.allocator = allocator;
        this.schedule = new Schedule(courses, professors, correlationMatrix);
        this.forcereassign = forcereassign;
    }

    public abstract void runSchedule();


    public Schedule getSchedule() {
        return this.schedule;
    }
    public String getName() {
        return this.name;
    }
    public Scoring getScoring() {
        return this.scoring;
    }
    public ConstraintValidator getConstraints() {
        return this.constraints;
    }
    public List<Course> getCourses() {
        return this.courses;
    }
    public List<Room> getRooms() {        
                return this.rooms;
    }
    public TypeBasedAllocation getAllocator() {
        return this.allocator;
    }
    public boolean isForceReassign() {
        return this.forcereassign;
    }
    public void setForceReassign(boolean forcereassign) {
        this.forcereassign = forcereassign;
    }
    
    @Override
    public String toString() {
        return "Scheduler: " + this.name + ", Scoring: " + this.scoring.toString() + ", Allocator: " + this.allocator.toString();
    }
}
