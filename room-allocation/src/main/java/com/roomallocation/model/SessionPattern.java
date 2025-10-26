package com.roomallocation.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the weekly pattern of sessions for a course
 * e.g., Mon/Wed/Fri at 10:00-11:30
 */
public class SessionPattern {
    private List<TimeSlot> sessions;
    private int sessionsPerWeek;

    public SessionPattern() {
        this.sessions = new ArrayList<>();
        this.sessionsPerWeek = 0;
    }

    /**
     * Add a session to the pattern
     */
    public void addSession(TimeSlot timeSlot) {
        sessions.add(timeSlot);
        sessionsPerWeek++;
    }

    /**
     * Add a session by day and time
     */
    public void addSession(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        sessions.add(new TimeSlot(day, startTime, endTime));
        sessionsPerWeek++;
    }

    /**
     * Check if pattern has any overlaps with another pattern
     */
    public boolean hasOverlapWith(SessionPattern other) {
        for (TimeSlot thisSlot : this.sessions) {
            for (TimeSlot otherSlot : other.sessions) {
                if (thisSlot.overlapsWith(otherSlot)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if pattern fits within professor availability
     */
    public boolean fitsAvailability(Professor professor) {
        for (TimeSlot session : sessions) {
            if (!professor.isAvailable(session.getDay(),
                                      session.getStartTime(),
                                      session.getEndTime())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get total contact hours per week
     */
    public int getTotalMinutesPerWeek() {
        return sessions.stream()
                      .mapToInt(TimeSlot::getDurationMinutes)
                      .sum();
    }

    /**
     * Check if all sessions are at the same time (different days)
     */
    public boolean hasSameTimeDifferentDays() {
        if (sessions.size() <= 1) {
            return true;
        }

        LocalTime firstStart = sessions.get(0).getStartTime();
        LocalTime firstEnd = sessions.get(0).getEndTime();

        for (int i = 1; i < sessions.size(); i++) {
            if (!sessions.get(i).getStartTime().equals(firstStart) ||
                !sessions.get(i).getEndTime().equals(firstEnd)) {
                return false;
            }
        }
        return true;
    }

    public List<TimeSlot> getSessions() {
        return new ArrayList<>(sessions);
    }

    public int getSessionsPerWeek() {
        return sessionsPerWeek;
    }

    public boolean isEmpty() {
        return sessions.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionPattern that = (SessionPattern) o;
        return Objects.equals(sessions, that.sessions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sessionsPerWeek).append(" sessions: ");
        for (int i = 0; i < sessions.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(sessions.get(i));
        }
        return sb.toString();
    }
}
