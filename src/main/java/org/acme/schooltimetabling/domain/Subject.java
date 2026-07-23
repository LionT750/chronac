package org.acme.schooltimetabling.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.smallrye.common.constraint.Nullable;

public class Subject {
    private final String name;
    private final int totalHours;
    private final String teacher;
    private @Nullable LocalDate startDate;
    private @Nullable LocalDate endDate;
    private List<String> designedRooms;
    private List<DayOfWeek> designDayOfWeeks;

    public Subject(String name, int totalHours, String teacher, LocalDate starDate, LocalDate endDate, List<String> rooms, List<DayOfWeek> days) {
        this.name = name;
        this.totalHours = totalHours;
        this.teacher = teacher;
        this.startDate = starDate;
        this.endDate = endDate;
        this.designedRooms = rooms == null ? new ArrayList<>() : new ArrayList<>(rooms);
        this.designDayOfWeeks = days == null ? new ArrayList<>() : new ArrayList<>(days);
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
        return totalHours;
    }

    public String getTeacher() {
        return teacher;
    }

    public List<DayOfWeek> getDesignDayOfWeeks() {
        return designDayOfWeeks;
    }

    public void setDesignDayOfWeeks(List<DayOfWeek> designDayOfWeeks) {
        this.designDayOfWeeks = designDayOfWeeks;
    }
}
