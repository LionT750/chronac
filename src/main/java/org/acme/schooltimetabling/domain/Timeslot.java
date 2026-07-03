package org.acme.schooltimetabling.domain;

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

    public Long getWeekOfYear() {
        return (long) date.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
