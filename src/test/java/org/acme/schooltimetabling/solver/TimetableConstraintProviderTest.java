package org.acme.schooltimetabling.solver;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Subject;
import org.acme.schooltimetabling.domain.Timetable;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Week;
import org.junit.jupiter.api.Test;

class TimetableConstraintProviderTest {

    private static final Room ROOM1 = new Room("1", "Sala 114");
    private static final Room ROOM2 = new Room("2", "Sala 115");

    private static final Timeslot MONDAY_W1 = new Timeslot("1", java.time.LocalDate.of(2026, 1, 5), DayOfWeek.MONDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot MONDAY_W2 = new Timeslot("2", java.time.LocalDate.of(2026, 1, 12), DayOfWeek.MONDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot TUESDAY_W1 = new Timeslot("3", java.time.LocalDate.of(2026, 1, 6), DayOfWeek.TUESDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot SATURDAY = new Timeslot("4", java.time.LocalDate.of(2026, 1, 10), DayOfWeek.SATURDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));

    private static final Subject SUBJECT_OOP = new Subject("OOP", 96, "Alisson", List.of("Sala 114"));
    private static final Subject SUBJECT_ALG = new Subject("Algoritmos", 108, "Rodolfo", List.of("Sala 114"));

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
        Lesson lesson3 = new Lesson("3", new Subject("BD", 72, "Nelma", List.of("Sala 114")));
        lesson3.setTimeslot(MONDAY_W1);
        lesson3.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomConflict)
                .given(lesson1, lesson2, lesson3)
                .penalizesBy(3);
    }

    @Test
    void roomPerSubject_whenRoomNotAllowed() {
        Subject subject = new Subject("OOP", 96, "Alisson", List.of("Sala 114"));
        Lesson lesson = new Lesson("1", subject);
        lesson.setTimeslot(MONDAY_W1);
        lesson.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomPerSubject)
                .given(lesson)
                .penalizesBy(1);
    }

    @Test
    void roomPerSubject_whenRoomAllowed_noPenalty() {
        Subject subject = new Subject("OOP", 96, "Alisson", List.of("Sala 114"));
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
    void dayOfWeekTeacherConsistency_twoDifferentTeachersOnSameDay() {
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", SUBJECT_ALG);
        lesson2.setTimeslot(MONDAY_W1);
        lesson2.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::dayOfWeekTeacherConsistency)
                .given(lesson1, lesson2)
                .penalizesBy(5);
    }

    @Test
    void dayOfWeekTeacherConsistency_sameTeacher_noPenalty() {
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", new Subject("OOP2", 48, "Alisson", List.of("Sala 114")));
        lesson2.setTimeslot(MONDAY_W1);
        lesson2.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::dayOfWeekTeacherConsistency)
                .given(lesson1, lesson2)
                .penalizesBy(0);
    }

    @Test
    void weeklyTeacherVariety_rewardsDistinctTeachersPerWeek() {
        Week week = new Week(2L);
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", SUBJECT_ALG);
        lesson2.setTimeslot(MONDAY_W1);
        lesson2.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weeklyTeacherVariety)
                .given(week, lesson1, lesson2)
                .rewardsWith(2);
    }

    @Test
    void weeklyTeacherVariety_sameTeacher_rewardsOne() {
        Week week = new Week(2L);
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", new Subject("OOP2", 48, "Alisson", List.of("Sala 114")));
        lesson2.setTimeslot(MONDAY_W1);
        lesson2.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weeklyTeacherVariety)
                .given(week, lesson1, lesson2)
                .rewardsWith(1);
    }

    @Test
    void fullWeekCoverage_twoDistinctDays_penalizes() {
        Week week = new Week(2L);
        Lesson mon = new Lesson("1", SUBJECT_OOP);
        mon.setTimeslot(MONDAY_W1);
        mon.setRoom(ROOM1);
        Lesson tue = new Lesson("2", SUBJECT_ALG);
        tue.setTimeslot(TUESDAY_W1);
        tue.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::fullWeekCoverage)
                .given(week, mon, tue)
                .penalizesBy(75);
    }

    @Test
    void fullWeekCoverage_oneDistinctDay_penalizes() {
        Week week = new Week(2L);
        Lesson mon = new Lesson("1", SUBJECT_OOP);
        mon.setTimeslot(MONDAY_W1);
        mon.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::fullWeekCoverage)
                .given(week, mon)
                .penalizesBy(100);
    }

    @Test
    void consecutiveWeeksSameWeekday_noNextWeek_penalizes() {
        Lesson lesson = new Lesson("1", SUBJECT_OOP);
        lesson.setTimeslot(MONDAY_W1);
        lesson.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::consecutiveWeeksSameWeekday)
                .given(lesson)
                .penalizesBy(25);
    }

    @Test
    void consecutiveWeeksSameWeekday_hasNextWeek_penalizesLast() {
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", SUBJECT_OOP);
        lesson2.setTimeslot(MONDAY_W2);
        lesson2.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::consecutiveWeeksSameWeekday)
                .given(lesson1, lesson2)
                .penalizesBy(25);
    }
}
