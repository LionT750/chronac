package org.acme.schooltimetabling.domain;

import java.util.Map;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class Curriculum {

    public Map<String, Subject> subjects;
    
    Curriculum() {
        this.subjects = Map.ofEntries(
                Map.entry("UC1", new Subject("UC1", 32, "Vanessa", LocalDate.of(2026, 7,22), LocalDate.of(2026, 12, 13), List.of("Sala 115"),
                        List.of(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC2", new Subject("UC2", 20, "Rodolfo", LocalDate.of(2026, 7,22), LocalDate.of(2026, 12, 13), List.of("Sala 115"),
                        List.of(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC3", new Subject("UC3", 20, "Alisson", LocalDate.of(2026, 7,22), LocalDate.of(2026, 12, 13), List.of("Sala 115"),
                        List.of(DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC4", new Subject("UC4", 72, "Alisson", LocalDate.of(2026, 7,22), LocalDate.of(2026, 12, 13), List.of("Sala 115"),
                        List.of(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC5", new Subject("UC5", 72, "Rodolfo", LocalDate.of(2026, 7,22), LocalDate.of(2026, 12, 13), List.of("Sala 115"),
                        List.of(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC6", new Subject("UC6", 24, "Alisson", LocalDate.of(2026, 7,22), LocalDate.of(2026, 12, 13), List.of("Sala 115"),
                        List.of(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC7", new Subject("UC7", 60, "Nelma", LocalDate.of(2026, 9,15), null,  List.of("Sala 114"),
                        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC8", new Subject("UC8", 84, "Vanessa",  LocalDate.of(2026, 9,15), null, List.of("Sala 114"),
                        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC9", new Subject("UC9", 108, "Rodolfo",  LocalDate.of(2026, 9,15), null, List.of("Sala 114"),
                        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC10", new Subject("UC10", 96, "Alisson",  LocalDate.of(2026, 9,15), null, List.of("Sala 114"),
                        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC11Al", new Subject("UC11Al", 42, "Alisson", LocalDate.of(2026, 9,15), null, List.of("Sala 114"),
                        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC11Van", new Subject("UC11Van", 42, "Vanessa",  LocalDate.of(2026, 9,15), null,List.of("Sala 114"),
                        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))),
                Map.entry("UC12", new Subject("UC12", 20, "Nelma", LocalDate.of(2026, 9,15), null, List.of("Sala 114"),
                        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)))
            );
    }
}
