package br.com.chronac.domain;

import io.smallrye.common.constraint.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Subject {
    private final String name;
    private @Nullable LocalDate startDate;
    private @Nullable LocalDate endDate;
    private List<String> designedRooms;
    private List<DayOfWeek> designDayOfWeeks;
    private List<DayOfWeek> effectiveDayOfWeeks;
    private Turma turma;
    /**
     * The teachers that deliver this UC, in teaching order. Most UCs have exactly
     * one part; a UC split between two teachers has one part each and is taught as
     * one continuous run that changes teacher halfway.
     */
    private final List<SubjectPart> parts = new ArrayList<>();

    /** A UC with no parts yet; call {@link #addPart} once per teacher, in order. */
    public Subject(String name, LocalDate starDate, LocalDate endDate, List<String> rooms, List<DayOfWeek> days) {
        this.name = name;
        this.startDate = starDate;
        this.endDate = endDate;
        this.designedRooms = rooms == null ? new ArrayList<>() : new ArrayList<>(rooms);
        this.designDayOfWeeks = days == null ? new ArrayList<>() : new ArrayList<>(days);
        this.effectiveDayOfWeeks = days == null ? new ArrayList<>() : new ArrayList<>(days);
    }

    /** The common single-teacher UC. */
    public Subject(String name, int totalHours, String teacher, LocalDate starDate, LocalDate endDate, List<String> rooms, List<DayOfWeek> days) {
        this(name, starDate, endDate, rooms, days);
        addPart(teacher, totalHours);
    }

    /** Appends the next teacher share of this UC; parts are taught in call order. */
    public Subject addPart(String teacher, int totalHours) {
        parts.add(new SubjectPart(this, parts.size(), teacher, totalHours));
        return this;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<SubjectPart> getParts() {
        return parts;
    }

    /** Distinct teachers of this UC, in teaching order. */
    public List<String> getTeachers() {
        return parts.stream().map(SubjectPart::getTeacher).distinct().toList();
    }

    public int getLessonCount() {
        return parts.stream().mapToInt(SubjectPart::getLessonCount).sum();
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate starDate) {
        this.startDate = starDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<String> getDesignedRooms() {
        return new ArrayList<>(designedRooms);
    }

    public void setDesignedRooms(List<String> designedRooms) {
        this.designedRooms = designedRooms == null ? new ArrayList<>() : new ArrayList<>(designedRooms);
    }

    public int getTotalHours() {
        return parts.stream().mapToInt(SubjectPart::getTotalHours).sum();
    }

    public List<DayOfWeek> getDesignDayOfWeeks() {
        return designDayOfWeeks;
    }

    public void setDesignDayOfWeeks(List<DayOfWeek> designDayOfWeeks) {
        this.designDayOfWeeks = designDayOfWeeks;
    }

    public List<DayOfWeek> getEffectiveDayOfWeeks() {
        return effectiveDayOfWeeks;
    }

    public void setEffectiveDayOfWeeks(List<DayOfWeek> effectiveDayOfWeeks) {
        this.effectiveDayOfWeeks = effectiveDayOfWeeks == null ? new ArrayList<>() : new ArrayList<>(effectiveDayOfWeeks);
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }
}