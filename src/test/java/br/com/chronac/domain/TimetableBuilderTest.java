package br.com.chronac.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class TimetableBuilderTest {

    private static Timetable build(LocalDate start, LocalDate end) {
        return new Timetable.Builder(start, end)
                .withName("Horizon")
                .withRooms(List.of(new Room("1", "Sala 114"), new Room("2", "Sala 115")))
                .build();
    }

    private static Map<String, Long> eligibleDaysPerRoom(Timetable timetable) {
        return timetable.getRooms().stream().collect(Collectors.toMap(
                Room::getName,
                room -> timetable.getTimeslots().stream()
                        .filter(timeslot -> timetable.getSubjects().stream()
                                .anyMatch(subject -> subject.getDesignedRooms().contains(room.getName())
                                        && subject.canHaveClassOn(timeslot.getDate())))
                        .count()));
    }

    private static Map<String, Long> lessonsPerRoom(Timetable timetable) {
        return timetable.getRooms().stream().collect(Collectors.toMap(
                Room::getName,
                room -> timetable.getSubjects().stream()
                        .filter(subject -> subject.getDesignedRooms().contains(room.getName()))
                        .mapToLong(subject -> subject.getTotalHours() / 4)
                        .sum()));
    }

    @Test
    void horizonOffersEveryRoomADayPerLesson() {
        Timetable timetable = build(LocalDate.of(2026, 7, 23), LocalDate.of(2027, 3, 8));
        Map<String, Long> eligible = eligibleDaysPerRoom(timetable);
        Map<String, Long> needed = lessonsPerRoom(timetable);

        for (Room room : timetable.getRooms()) {
            String name = room.getName();
            assertTrue(eligible.get(name) >= needed.get(name),
                    name + " has " + eligible.get(name) + " eligible days for " + needed.get(name) + " lessons");
        }
    }

    @Test
    void horizonStopsBeforeTheSemesterEndWhenHoursRunOutFirst() {
        LocalDate semesterEnd = LocalDate.of(2027, 3, 8);
        Timetable timetable = build(LocalDate.of(2026, 7, 23), semesterEnd);

        LocalDate lastDay = timetable.getTimeslots().stream()
                .map(Timeslot::getDate)
                .max(LocalDate::compareTo)
                .orElseThrow();

        assertTrue(lastDay.isBefore(semesterEnd),
                "expected the horizon to stop before " + semesterEnd + " but it reached " + lastDay);
    }

    @Test
    void horizonIsStillCappedBySemesterEndWhenHoursDoNotFit() {
        Timetable timetable = build(LocalDate.of(2026, 7, 23), LocalDate.of(2026, 10, 30));

        LocalDate lastDay = timetable.getTimeslots().stream()
                .map(Timeslot::getDate)
                .max(LocalDate::compareTo)
                .orElseThrow();

        assertEquals(LocalDate.of(2026, 10, 30), lastDay);
    }
}
