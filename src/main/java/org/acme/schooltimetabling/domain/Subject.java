package org.acme.schooltimetabling.domain;

public class Subject {
    private final String name;
    private final int totalHours;
    private final String teacher;

    public Subject(String name, int totalHours, String teacher) {
        this.name = name;
        this.totalHours = totalHours;
        this.teacher = teacher;
    }

    public String getName() {
        return name;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public String getTeacher() {
        return teacher;
    }
}
