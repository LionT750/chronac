package org.acme.schooltimetabling.domain;

import java.util.ArrayList;
import java.util.List;

public class Subject {
    private final String name;
    private final int totalHours;
    private final String teacher;
    private List<String> designedRooms;

    public Subject(String name, int totalHours, String teacher, List<String> rooms) {
        this.name = name;
        this.totalHours = totalHours;
        this.teacher = teacher;
        this.designedRooms = rooms == null ? new ArrayList<>() : new ArrayList<>(rooms);
    }

    public String getName() {
        return name;
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
}
