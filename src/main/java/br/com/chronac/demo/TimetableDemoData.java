package br.com.chronac.demo;

import br.com.chronac.domain.Room;
import br.com.chronac.domain.TeacherSchedule;
import br.com.chronac.domain.Timetable;
import br.com.chronac.domain.Teacher;

import java.time.LocalDate;
import java.util.List;

/**
 * Builds the exact same "MultiTurma Demo" problem that the legacy main() solved
 * and exposed via the raw HttpServer. Teacher schedule restrictions now come
 * from the persisted {@link Teacher} rows (seeded by {@link TeacherDataSeeder})
 * instead of being built inline.
 */
public final class TimetableDemoData {

    private TimetableDemoData() {
    }

    public static Timetable buildDemoProblem(List<Teacher> teachers) {
        List<TeacherSchedule> teacherSchedules = teachers.stream()
                .map(TimetableDemoData::toTeacherSchedule)
                .toList();

        return new Timetable.Builder(LocalDate.of(2026, 7, 23), LocalDate.of(2027, 3, 8))
                .withName("MultiTurma Demo")
                .withRooms(List.of(
                        new Room(Long.toString(1L), "Sala 114"),
                        new Room(Long.toString(2L), "Sala 115")))
                .withTeacherSchedules(teacherSchedules)
                .build();
    }

    private static TeacherSchedule toTeacherSchedule(Teacher teacher) {
        TeacherSchedule schedule = new TeacherSchedule(teacher.getName());
        teacher.getInvalidDayOfWeeks().forEach(schedule::addInvalidDayOfWeek);
        teacher.getSpecificUnavailableDates().forEach(schedule::addSpecificUnavailableDate);
        return schedule;
    }
}