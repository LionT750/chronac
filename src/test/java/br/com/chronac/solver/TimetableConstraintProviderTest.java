package br.com.chronac.solver;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;
import br.com.chronac.domain.Lesson;
import br.com.chronac.domain.Room;
import br.com.chronac.domain.Subject;
import br.com.chronac.domain.Timetable;
import br.com.chronac.domain.Timeslot;
import org.junit.jupiter.api.Test;

class TimetableConstraintProviderTest {

    private static final Room ROOM1 = new Room("1", "Sala 114");
    private static final Room ROOM2 = new Room("2", "Sala 115");

    private static final Timeslot MONDAY_W1 = new Timeslot("1", LocalDate.of(2026, 1, 5), DayOfWeek.MONDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot MONDAY_W2 = new Timeslot("2", LocalDate.of(2026, 1, 12), DayOfWeek.MONDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot MONDAY_W3 = new Timeslot("5", LocalDate.of(2026, 1, 19), DayOfWeek.MONDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot TUESDAY_W1 = new Timeslot("3", LocalDate.of(2026, 1, 6), DayOfWeek.TUESDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot TUESDAY_W2 = new Timeslot("7", LocalDate.of(2026, 1, 13), DayOfWeek.TUESDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot WEDNESDAY_W1 = new Timeslot("6", LocalDate.of(2026, 1, 7), DayOfWeek.WEDNESDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot SATURDAY = new Timeslot("4", LocalDate.of(2026, 1, 10), DayOfWeek.SATURDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));

    private static final Subject SUBJECT_OOP = subject("OOP", "Alisson", LocalDate.of(2026, 9, 15), null, List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
    private static final Subject SUBJECT_ALG = subject("Algoritmos", "Rodolfo", LocalDate.of(2026, 9, 15), null, List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

    private static Subject subject(String name, String teacher, LocalDate start, LocalDate end, List<DayOfWeek> days) {
        return new Subject(name, 96, teacher, start, end, List.of("Sala 114"), days);
    }

    ConstraintVerifier<TimetableConstraintProvider, Timetable> constraintVerifier = ConstraintVerifier.build(
            new TimetableConstraintProvider(), Timetable.class, Lesson.class);

    @Test
    void roomConflict_whenSameTimeslotAndSameRoom() {
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", SUBJECT_ALG);
        lesson2.setTimeslot(MONDAY_W1);
        lesson2.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomConflict)
                .given(lesson1, lesson2)
                .penalizesBy(1);
    }

    @Test
    void roomConflict_whenDifferentRoom_noPenalty() {
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", SUBJECT_ALG);
        lesson2.setTimeslot(MONDAY_W1);
        lesson2.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomConflict)
                .given(lesson1, lesson2)
                .penalizesBy(0);
    }

    @Test
    void roomConflict_whenDifferentTimeslot_noPenalty() {
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", SUBJECT_ALG);
        lesson2.setTimeslot(MONDAY_W2);
        lesson2.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomConflict)
                .given(lesson1, lesson2)
                .penalizesBy(0);
    }

    @Test
    void roomConflict_threeLessonsSameSlotAndRoom_threePairs() {
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", SUBJECT_ALG);
        lesson2.setTimeslot(MONDAY_W1);
        lesson2.setRoom(ROOM1);
        Lesson lesson3 = new Lesson("3", new Subject("BD", 72, "Nelma", LocalDate.of(2026, 9, 15), null, List.of("Sala 114"), List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)));
        lesson3.setTimeslot(MONDAY_W1);
        lesson3.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomConflict)
                .given(lesson1, lesson2, lesson3)
                .penalizesBy(3);
    }

    @Test
    void roomPerSubject_whenRoomNotAllowed() {
        Subject subject = new Subject("OOP", 96, "Alisson", LocalDate.of(2026, 9, 15), null, List.of("Sala 114"), List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        Lesson lesson = new Lesson("1", subject);
        lesson.setTimeslot(MONDAY_W1);
        lesson.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomPerSubject)
                .given(lesson)
                .penalizesBy(1);
    }

    @Test
    void roomPerSubject_whenRoomAllowed_noPenalty() {
        Subject subject = new Subject("OOP", 96, "Alisson", LocalDate.of(2026, 9, 15), null, List.of("Sala 114"), List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        Lesson lesson = new Lesson("1", subject);
        lesson.setTimeslot(MONDAY_W1);
        lesson.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomPerSubject)
                .given(lesson)
                .penalizesBy(0);
    }

    @Test
    void daysWithoutClass_onWeekend_penalizes() {
        Lesson lesson = new Lesson("1", SUBJECT_OOP);
        lesson.setTimeslot(SATURDAY);
        lesson.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::daysWithoutClass)
                .given(lesson)
                .penalizesBy(1);
    }

    @Test
    void daysWithoutClass_onWeekday_noPenalty() {
        Lesson lesson = new Lesson("1", SUBJECT_OOP);
        lesson.setTimeslot(MONDAY_W1);
        lesson.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::daysWithoutClass)
                .given(lesson)
                .penalizesBy(0);
    }

    @Test
    void subjectCompaction_lessonOnStartDate_noPenalty() {
        Subject subject = new Subject("OOP", 96, "Alisson", LocalDate.of(2026, 1, 5), null, List.of("Sala 114"), List.of(DayOfWeek.MONDAY));
        subject.setWeeklyCadenceCap(1);
        Lesson lesson = new Lesson("1", subject);
        lesson.setTimeslot(MONDAY_W1);
        lesson.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::subjectCompaction)
                .given(lesson)
                .penalizesBy(0);
    }

    @Test
    void subjectCompaction_lessonTwoWeeksAfterStart_penalizesDays() {
        Subject subject = new Subject("OOP", 96, "Alisson", LocalDate.of(2026, 1, 5), null, List.of("Sala 114"), List.of(DayOfWeek.MONDAY));
        subject.setWeeklyCadenceCap(1);
        Lesson lesson = new Lesson("1", subject);
        lesson.setTimeslot(MONDAY_W3);
        lesson.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::subjectCompaction)
                .given(lesson)
                .penalizesBy(14);
    }

    @Test
    void cadenceOverrun_lessonsBeyondCap_penalizesExcess() {
        Subject subject = SUBJECT_OOP;
        subject.setWeeklyCadenceCap(2);
        Lesson monday = new Lesson("1", subject);
        monday.setTimeslot(MONDAY_W1);
        monday.setRoom(ROOM1);
        Lesson tuesday = new Lesson("2", subject);
        tuesday.setTimeslot(TUESDAY_W1);
        tuesday.setRoom(ROOM2);
        Lesson wednesday = new Lesson("3", subject);
        wednesday.setTimeslot(WEDNESDAY_W1);
        wednesday.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::cadenceOverrun)
                .given(monday, tuesday, wednesday)
                .penalizesBy(20);
    }

    @Test
    void cadenceOverrun_withinCap_noPenalty() {
        Subject subject = SUBJECT_OOP;
        subject.setWeeklyCadenceCap(3);
        Lesson monday = new Lesson("1", subject);
        monday.setTimeslot(MONDAY_W1);
        monday.setRoom(ROOM1);
        Lesson tuesday = new Lesson("2", subject);
        tuesday.setTimeslot(TUESDAY_W1);
        tuesday.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::cadenceOverrun)
                .given(monday, tuesday)
                .penalizesBy(0);
    }

    @Test
    void cadenceSteadiness_partialActiveWeek_penalizes() {
        Subject subject = SUBJECT_OOP;
        subject.setWeeklyCadenceCap(2);
        Lesson monday = new Lesson("1", subject);
        monday.setTimeslot(MONDAY_W1);
        monday.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::cadenceSteadiness)
                .given(monday)
                .penalizesBy(8);
    }

    @Test
    void cadenceSteadiness_fullWeek_noPenalty() {
        Subject subject = SUBJECT_OOP;
        subject.setWeeklyCadenceCap(2);
        Lesson monday = new Lesson("1", subject);
        monday.setTimeslot(MONDAY_W1);
        monday.setRoom(ROOM1);
        Lesson tuesday = new Lesson("2", subject);
        tuesday.setTimeslot(TUESDAY_W1);
        tuesday.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::cadenceSteadiness)
                .given(monday, tuesday)
                .penalizesBy(0);
    }

    @Test
    void weekdayStability_sameWeekdayFromWeekToWeek_noPenalty() {
        Subject subject = SUBJECT_OOP;
        subject.setWeeklyCadenceCap(1);
        Lesson monday1 = new Lesson("1", subject);
        monday1.setTimeslot(MONDAY_W1);
        monday1.setRoom(ROOM1);
        Lesson monday2 = new Lesson("2", subject);
        monday2.setTimeslot(MONDAY_W2);
        monday2.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weekdayStability)
                .given(monday1, monday2)
                .penalizesBy(0);
    }

    @Test
    void weekdayStability_switchesWeekdayMidBlock_penalizes() {
        Subject subject = SUBJECT_OOP;
        subject.setWeeklyCadenceCap(1);
        Lesson monday = new Lesson("1", subject);
        monday.setTimeslot(MONDAY_W1);
        monday.setRoom(ROOM1);
        Lesson tuesday = new Lesson("2", subject);
        tuesday.setTimeslot(TUESDAY_W2);
        tuesday.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weekdayStability)
                .given(monday, tuesday)
                .penalizesBy(25);
    }

    @Test
    void weekdayStability_switchesToEarlierWeekdayInNextWeek_penalizes() {
        Subject subject = SUBJECT_OOP;
        subject.setWeeklyCadenceCap(1);
        Lesson wednesday = new Lesson("1", subject);
        wednesday.setTimeslot(WEDNESDAY_W1);
        wednesday.setRoom(ROOM1);
        Lesson monday = new Lesson("2", subject);
        monday.setTimeslot(MONDAY_W2);
        monday.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weekdayStability)
                .given(wednesday, monday)
                .penalizesBy(25);
    }

    @Test
    void roomFill_emptySlotInsideActiveSpan_penalizes() {
        Lesson monday1 = new Lesson("1", SUBJECT_OOP);
        monday1.setTimeslot(MONDAY_W1);
        monday1.setRoom(ROOM1);
        Lesson monday3 = new Lesson("2", SUBJECT_OOP);
        monday3.setTimeslot(MONDAY_W3);
        monday3.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomFill)
                .given(MONDAY_W1, MONDAY_W2, MONDAY_W3, ROOM1, monday1, monday3)
                .penalizesBy(20);
    }

    @Test
    void roomFill_fullSpan_noPenalty() {
        Lesson monday1 = new Lesson("1", SUBJECT_OOP);
        monday1.setTimeslot(MONDAY_W1);
        monday1.setRoom(ROOM1);
        Lesson monday2 = new Lesson("2", SUBJECT_ALG);
        monday2.setTimeslot(MONDAY_W2);
        monday2.setRoom(ROOM1);
        Lesson monday3 = new Lesson("3", SUBJECT_OOP);
        monday3.setTimeslot(MONDAY_W3);
        monday3.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomFill)
                .given(MONDAY_W1, MONDAY_W2, MONDAY_W3, ROOM1, monday1, monday2, monday3)
                .penalizesBy(0);
    }

    @Test
    void roomFill_emptyTail_noPenalty() {
        Lesson monday1 = new Lesson("1", SUBJECT_OOP);
        monday1.setTimeslot(MONDAY_W1);
        monday1.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomFill)
                .given(MONDAY_W1, MONDAY_W2, MONDAY_W3, ROOM1, monday1)
                .penalizesBy(0);
    }

    @Test
    void roomFill_weekdayDropsOutWhileRoomStillActive_penalizes() {
        Lesson monday1 = new Lesson("1", SUBJECT_OOP);
        monday1.setTimeslot(MONDAY_W1);
        monday1.setRoom(ROOM1);
        Lesson monday3 = new Lesson("2", SUBJECT_OOP);
        monday3.setTimeslot(MONDAY_W3);
        monday3.setRoom(ROOM1);
        Lesson tuesday = new Lesson("3", SUBJECT_ALG);
        tuesday.setTimeslot(TUESDAY_W2);
        tuesday.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomFill)
                .given(MONDAY_W1, MONDAY_W2, MONDAY_W3, TUESDAY_W2, ROOM1, monday1, monday3, tuesday)
                .penalizesBy(20);
    }
}