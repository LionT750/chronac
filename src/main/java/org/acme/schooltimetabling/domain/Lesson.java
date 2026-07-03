package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Lesson {

    @PlanningId
    private String id;

    private String subject;
    private String teacher;

    @PlanningVariable
    private Timeslot timeslot;

    @PlanningVariable
    private Room room;

    // No-arg constructor required for Timefold
    public Lesson() {
    }

    public Lesson(String id, String subject, String teacher) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
    }

    public Lesson(String id, String subject, String teacher, Timeslot timeslot, Room room) {
        this(id, subject, teacher);
        this.timeslot = timeslot;
        this.room = room;
    }

    @Override
    public String toString() {
        return subject + "(" + id + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

}
