package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Lesson {

    @PlanningId
    private String id;

    private Subject subject;

    @PlanningVariable
    private Timeslot timeslot;

    @PlanningVariable
    private Room room;

    // Required by Timefold
    public Lesson() {
    }

    public Lesson(String id, Subject subject) {
        this.id = id;
        this.subject = subject;
    }

    @Override
    public String toString() {
        return subject.getName() + "(" + id + ")";
    }

    // ************************************************************************
    // Getters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    public String getTeacher() {
        return subject.getTeacher();
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public Room getRoom() {
        return room;
    }

    // ************************************************************************
    // Setters
    // ************************************************************************

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}