package org.acme.schooltimetabling.domain;

import java.util.List;

public class Subject {
    private final String name;
    private final int totalHours;
    private final String teacher;
    public List<String> designedRooms;

    public Subject(String name, int totalHours, String teacher, List<String> rooms) {
        this.name = name;
        this.totalHours = totalHours;
        this.teacher = teacher;
        this.designedRooms = rooms;
    }

    public String getName() {
        return name;
    }

    public List<String> getDesignedRooms() {
        return designedRooms;
    }

    public void setDesignedRooms(List<String> designedRooms) {
        this.designedRooms = designedRooms;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public String getTeacher() {
        return teacher;
    }
}
