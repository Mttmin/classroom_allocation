package com.roomallocation.scheduler.optimizer;

import com.roomallocation.scheduler.scoring.Scoring;
import com.roomallocation.constraint.ConstraintValidator;

public class NaiveScheduler extends Scheduler {
    public void runSchedule() {
        // Naive scheduling logic implementation
    }
    public NaiveScheduler(String name, Scoring scoring, ConstraintValidator constraints, java.util.List<com.roomallocation.model.Course> courses, java.util.List<com.roomallocation.model.Room> rooms, com.roomallocation.allocation.TypeBasedAllocation allocator, boolean forcereassign) {
        super(name, scoring, constraints, courses, rooms, allocator, forcereassign);
    }
}
