package br.com.chronac.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class Timeslot {

    @PlanningId
    private String id;

    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public Timeslot() {
    }

    public Timeslot(String id, LocalDate date, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return date + " " + dayOfWeek + " " + startTime;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * A monotonically increasing week index (epoch weeks, Monday-based) rather than
     * an ISO week-of-year. Consecutive calendar weeks always differ by exactly 1,
     * so week arithmetic (spans, adjacency) stays correct across the New Year - an
     * ISO week-of-year wraps 53 -> 1 and breaks it.
     */
    public Long getWeekIndex() {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toEpochDay() / 7;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}