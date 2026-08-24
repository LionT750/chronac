package br.com.chronac.domain;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A turma (cohort) groups the subjects that a single class of students follows
 * together - e.g. "Jovem Programador" or "Técnico em Desenvolvimento de
 * Sistemas". Every subject belongs to exactly one turma, and a turma has a home
 * set of rooms and a fixed roster of teachers (derived from its subjects).
 *
 * Turmas are problem facts (not planning entities): the solver never changes
 * which turma a subject belongs to, it only arranges the lessons.
 */
public class Turma {

    private final String name;
    private final List<String> rooms;
    private final List<Subject> subjects;
    private long firstViableWeek;

    public Turma(String name, List<String> rooms, List<Subject> subjects) {
        this.name = name;
        this.rooms = rooms == null ? List.of() : List.copyOf(rooms);
        this.subjects = subjects == null ? List.of() : List.copyOf(subjects);
    }

    public String getName() {
        return name;
    }

    public List<String> getRooms() {
        return rooms;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<Subject> getSubjects() {
        return subjects;
    }

    /** Distinct teachers that teach this turma's subjects. */
    public Set<String> getDistinctTeachers() {
        Set<String> teachers = new LinkedHashSet<>();
        for (Subject subject : subjects) {
            teachers.addAll(subject.getTeachers());
        }
        return teachers;
    }

    public int getTeacherCount() {
        return getDistinctTeachers().size();
    }

    /**
     * The first week in which it is arithmetically possible for every teacher of
     * this turma to hold an evening - the first week offering at least as many
     * class days as the turma has teachers. Jovem Programador opens on a Thursday
     * with only two evenings for three teachers, so its real opening week is the
     * one after. Set by the Timetable builder once the calendar is known.
     */
    public long getFirstViableWeek() {
        return firstViableWeek;
    }

    public void setFirstViableWeek(long firstViableWeek) {
        this.firstViableWeek = firstViableWeek;
    }

    @Override
    public String toString() {
        return name;
    }
}
