package br.com.chronac.domain;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class Curriculum {

    public Map<String, Subject> subjects;
    private final List<Turma> turmas;
    private Map<String, TeacherSchedule> teacherSchedules = new HashMap<>();

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
                // One UC of 84h split between two teachers and taught as a single
                // continuous run: Alisson's 42h first, then Vanessa closes it out.
                // Both parts share one weekday track back to back, so from the
                // students' side it is one UC that changes teacher halfway.
                Map.entry("UC11", new Subject("UC11", LocalDate.of(2026, 9,15), null, List.of("Sala 114"),
                        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))
                        .addPart("Alisson", 42)
                        .addPart("Vanessa", 42)),
                Map.entry("UC12", new Subject("UC12", 20, "Nelma", LocalDate.of(2026, 9,15), null, List.of("Sala 114"),
                        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)))
            );

        // Group the subjects into turmas (cohorts). UC1-UC6 share Sala 115 and
        // belong to "Jovem Programador"; UC7-UC12 share Sala 114 and belong to
        // "Técnico em Desenvolvimento de Sistemas".
        Turma jovemProgramador = new Turma("Jovem Programador", List.of("Sala 115"),
                List.of("UC1", "UC2", "UC3", "UC4", "UC5", "UC6").stream()
                        .map(subjects::get).toList());
        Turma tecnicoDesenvolvimento = new Turma("Técnico em Desenvolvimento de Sistemas", List.of("Sala 114"),
                List.of("UC7", "UC8", "UC9", "UC10", "UC11", "UC12").stream()
                        .map(subjects::get).toList());

        for (Subject subject : jovemProgramador.getSubjects()) {
            subject.setTurma(jovemProgramador);
        }
        for (Subject subject : tecnicoDesenvolvimento.getSubjects()) {
            subject.setTurma(tecnicoDesenvolvimento);
        }

        this.turmas = List.of(jovemProgramador, tecnicoDesenvolvimento);
    }

    public List<Turma> getTurmas() {
        return turmas;
    }

    public void registerTeacherSchedule(TeacherSchedule schedule) {
        teacherSchedules.put(schedule.getTeacherName(), schedule);
    }

    public TeacherSchedule getTeacherSchedule(String teacherName) {
        return teacherSchedules.get(teacherName);
    }

    public Map<String, TeacherSchedule> getTeacherSchedules() {
        return teacherSchedules;
    }

    public List<DayOfWeek> getValidDayOfWeeksForSubject(String subjectName) {
        Subject subject = subjects.get(subjectName);
        if (subject == null) {
            return List.of();
        }

        return availableDayOfWeeks(subject);
    }

    public void applyTeacherSchedules() {
        for (Subject subject : subjects.values()) {
            subject.setEffectiveDayOfWeeks(availableDayOfWeeks(subject));
        }
    }

    /**
     * The weekdays a UC can actually be taught on: its designed weekdays minus the
     * weekdays any of its teachers cannot work. It is an intersection across all
     * parts because every part of a UC shares one weekday track, so the weekday
     * has to suit each of them - UC11 is only teachable on a day both Alisson and
     * Vanessa can work.
     */
    private List<DayOfWeek> availableDayOfWeeks(Subject subject) {
        return subject.getDesignDayOfWeeks().stream()
                .filter(day -> subject.getTeachers().stream()
                        .map(teacherSchedules::get)
                        .allMatch(schedule -> schedule == null || schedule.isDayOfWeekAvailable(day)))
                .toList();
    }

    public boolean isTeacherAvailableOnDate(String teacherName, LocalDate date) {
        TeacherSchedule schedule = teacherSchedules.get(teacherName);
        if (schedule == null) {
            return true;
        }
        return schedule.isDateAvailable(date);
    }

    public List<LocalDate> getTeacherUnavailableDates(String teacherName) {
        TeacherSchedule schedule = teacherSchedules.get(teacherName);
        if (schedule == null) {
            return List.of();
        }
        return schedule.getSpecificUnavailableDates();
    }

    public void clearAllTeacherSchedules() {
        teacherSchedules.clear();
    }
}