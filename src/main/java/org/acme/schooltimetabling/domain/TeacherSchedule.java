package org.acme.schooltimetabling.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TeacherSchedule {

    private String teacherName;
    private List<DayOfWeek> invalidDayOfWeeks;
    private List<LocalDate> specificUnavailableDates;
    private List<DateRange> unavailableDateRanges;

    public TeacherSchedule() {
    }

    public TeacherSchedule(String teacherName) {
        this.teacherName = teacherName;
        this.invalidDayOfWeeks = new ArrayList<>();
        this.specificUnavailableDates = new ArrayList<>();
        this.unavailableDateRanges = new ArrayList<>();
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public List<DayOfWeek> getInvalidDayOfWeeks() {
        return invalidDayOfWeeks == null ? List.of() : invalidDayOfWeeks;
    }

    public void setInvalidDayOfWeeks(List<DayOfWeek> invalidDayOfWeeks) {
        this.invalidDayOfWeeks = invalidDayOfWeeks == null ? new ArrayList<>() : new ArrayList<>(invalidDayOfWeeks);
    }

    public void addInvalidDayOfWeek(DayOfWeek day) {
        if (invalidDayOfWeeks == null) {
            invalidDayOfWeeks = new ArrayList<>();
        }
        if (!invalidDayOfWeeks.contains(day)) {
            invalidDayOfWeeks.add(day);
        }
    }

    public List<LocalDate> getSpecificUnavailableDates() {
        return specificUnavailableDates == null ? List.of() : specificUnavailableDates;
    }

    public void setSpecificUnavailableDates(List<LocalDate> specificUnavailableDates) {
        this.specificUnavailableDates = specificUnavailableDates == null ? new ArrayList<>() : new ArrayList<>(specificUnavailableDates);
    }

    public void addSpecificUnavailableDate(LocalDate date) {
        if (specificUnavailableDates == null) {
            specificUnavailableDates = new ArrayList<>();
        }
        if (!specificUnavailableDates.contains(date)) {
            specificUnavailableDates.add(date);
        }
    }

    public List<DateRange> getUnavailableDateRanges() {
        return unavailableDateRanges == null ? List.of() : unavailableDateRanges;
    }

    public void setUnavailableDateRanges(List<DateRange> unavailableDateRanges) {
        this.unavailableDateRanges = unavailableDateRanges == null ? new ArrayList<>() : new ArrayList<>(unavailableDateRanges);
    }

    public void addUnavailableDateRange(DateRange range) {
        if (unavailableDateRanges == null) {
            unavailableDateRanges = new ArrayList<>();
        }
        unavailableDateRanges.add(range);
    }

    public boolean isDayOfWeekAvailable(DayOfWeek day) {
        return !getInvalidDayOfWeeks().contains(day);
    }

    public boolean isDateAvailable(LocalDate date) {
        if (getSpecificUnavailableDates().contains(date)) {
            return false;
        }
        for (DateRange range : getUnavailableDateRanges()) {
            if (range.contains(date)) {
                return false;
            }
        }
        return true;
    }
}
