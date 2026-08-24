package br.com.chronac.domain;

/**
 * One evening of one UC, on a date, in a room.
 *
 * Lessons are no longer planned: {@link Block} is the planning entity and a
 * lesson is what a block materializes into once it knows where it starts. This
 * class exists so the API and the UI keep seeing the flat list of dated lessons
 * they always saw, while the solver reasons about contiguous runs.
 */
public class Lesson {

    private String id;

    private SubjectPart part;

    private Timeslot timeslot;

    private Room room;

    // Required by Jackson
    public Lesson() {
    }

    public Lesson(String id, SubjectPart part, Timeslot timeslot, Room room) {
        this.id = id;
        this.part = part;
        this.timeslot = timeslot;
        this.room = room;
    }

    @Override
    public String toString() {
        return getSubject().getName() + "(" + id + ")";
    }

    // ************************************************************************
    // Getters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public Subject getSubject() {
        return part.getSubject();
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public SubjectPart getPart() {
        return part;
    }

    public String getTeacher() {
        return part.getTeacher();
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public Room getRoom() {
        return room;
    }
}
