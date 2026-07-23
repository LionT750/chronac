package org.acme.schooltimetabling.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Semester {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<LocalDate> holidays = new ArrayList<>();
    private List<LocalDate> validClassDays;

    private Curriculum curriculum = new Curriculum();

    public Semester(LocalDate startDate, LocalDate endDate, List<LocalDate> holidays) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.holidays = holidays == null
        ? new ArrayList<>()
        : new ArrayList<>(holidays);
        this.validClassDays = generateValidClassDays();
    }

    private List<LocalDate> generateValidClassDays() {
        List<LocalDate> validClassDays = new java.util.ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (!holidays.contains(currentDate) && !isWeekend(currentDate)) {
                validClassDays.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
        return validClassDays;
    }

    // Getters and setters

    public List<LocalDate> getValidClassDays() {
        return validClassDays;
    }

    public List<LocalDate> getHolidays() {
        return holidays;
    }

    public void addHoliday(LocalDate holiday) {
        if (!holidays.contains(holiday)) {
            holidays.add(holiday);
            recomputeValidDays();
        }
    }

    public void removeHoliday(LocalDate holiday) {
        if (holidays.remove(holiday)) {
            recomputeValidDays();
        }
    }

    public void recomputeValidDays() {
        this.validClassDays = generateValidClassDays();
    }

    public Integer getSubjectHours(String subject) {
        return curriculum.subjects.getOrDefault(subject, new Subject(subject, 0, "", null)).getTotalHours();
    }

    public Curriculum getCurriculum() {
        return curriculum;
    }

    //UTIL METHODS

    public boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}

