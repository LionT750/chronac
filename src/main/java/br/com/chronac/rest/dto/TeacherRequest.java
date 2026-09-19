package br.com.chronac.rest.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public record TeacherRequest(
        String name,
        String email,
        String phone,
        Set<DayOfWeek> invalidDayOfWeeks,
        Set<LocalDate> specificUnavailableDates) {
}
