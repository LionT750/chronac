package br.com.chronac.demo;

import br.com.chronac.domain.Room;
import br.com.chronac.domain.TeacherSchedule;
import br.com.chronac.domain.Timetable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Builds the exact same "MultiTurma Demo" problem that the legacy main() solved
 * and exposed via the raw HttpServer.
 */
public final class TimetableDemoData {

    private TimetableDemoData() {
    }

    public static Timetable buildDemoProblem() {
        TeacherSchedule nelmaSchedule = new TeacherSchedule("Nelma");
        nelmaSchedule.addInvalidDayOfWeek(DayOfWeek.WEDNESDAY);
        nelmaSchedule.addInvalidDayOfWeek(DayOfWeek.THURSDAY);

        TeacherSchedule rodolfoSchedule = new TeacherSchedule("Rodolfo");
        rodolfoSchedule.addInvalidDayOfWeek(DayOfWeek.FRIDAY);

        TeacherSchedule vanessaSchedule = new TeacherSchedule("Vanessa");
        vanessaSchedule.addUnavailableDateRange(LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 30));
        vanessaSchedule.addSpecificUnavailableDate(LocalDate.of(2026, 7, 2));
        vanessaSchedule.addSpecificUnavailableDate(LocalDate.of(2026, 7, 9));
        vanessaSchedule.addSpecificUnavailableDate(LocalDate.of(2026, 7, 16));
        vanessaSchedule.addSpecificUnavailableDate(LocalDate.of(2026, 8, 6));
        vanessaSchedule.addSpecificUnavailableDate(LocalDate.of(2026, 8, 13));
        vanessaSchedule.addSpecificUnavailableDate(LocalDate.of(2026, 8, 20));
        vanessaSchedule.addSpecificUnavailableDate(LocalDate.of(2026, 8, 27));

        return new Timetable.Builder(LocalDate.of(2026, 7, 23), LocalDate.of(2027, 3, 8))
                .withName("MultiTurma Demo")
                .withRooms(List.of(
                        new Room(Long.toString(1L), "Sala 114"),
                        new Room(Long.toString(2L), "Sala 115")))
                .withTeacherSchedules(List.of(nelmaSchedule, rodolfoSchedule, vanessaSchedule))
                .build();
    }
}