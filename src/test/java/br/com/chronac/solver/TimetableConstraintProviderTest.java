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
import br.com.chronac.domain.Week;
import org.junit.jupiter.api.Test;

class TimetableConstraintProviderTest {

    private static final Room ROOM1 = new Room("1", "Sala 114");
    private static final Room ROOM2 = new Room("2", "Sala 115");

    private static final Timeslot MONDAY_W1 = new Timeslot("1", java.time.LocalDate.of(2026, 1, 5), DayOfWeek.MONDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot MONDAY_W2 = new Timeslot("2", java.time.LocalDate.of(2026, 1, 12), DayOfWeek.MONDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot TUESDAY_W1 = new Timeslot("3", java.time.LocalDate.of(2026, 1, 6), DayOfWeek.TUESDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot SATURDAY = new Timeslot("4", java.time.LocalDate.of(2026, 1, 10), DayOfWeek.SATURDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot MONDAY_IN_WINDOW = new Timeslot("5", java.time.LocalDate.of(2026, 9, 21), DayOfWeek.MONDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot MONDAY_BEFORE_WINDOW = new Timeslot("6", java.time.LocalDate.of(2026, 6, 1), DayOfWeek.MONDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot TUESDAY_IN_WINDOW = new Timeslot("7", java.time.LocalDate.of(2026, 9, 22), DayOfWeek.TUESDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot WEDNESDAY_IN_WINDOW = new Timeslot("8", java.time.LocalDate.of(2026, 9, 23), DayOfWeek.WEDNESDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot TUESDAY_W2 = new Timeslot("9", java.time.LocalDate.of(2026, 1, 13), DayOfWeek.TUESDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    private static final Timeslot MONDAY_W3 = new Timeslot("10", java.time.LocalDate.of(2026, 1, 19), DayOfWeek.MONDAY, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));

    private static final Subject SUBJECT_OOP = new Subject("OOP", 96, "Alisson", LocalDate.of(2026, 9,15), null, List.of("Sala 114"), List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
    private static final Subject SUBJECT_ALG = new Subject("Algoritmos", 108, "Rodolfo", LocalDate.of(2026, 9,15), null, List.of("Sala 114"), List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

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
        Lesson lesson3 = new Lesson("3", new Subject("BD", 72, "Nelma", LocalDate.of(2026, 9,15), null, List.of("Sala 114"),List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)));
        lesson3.setTimeslot(MONDAY_W1);
        lesson3.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomConflict)
                .given(lesson1, lesson2, lesson3)
                .penalizesBy(3);
    }

    @Test
    void roomPerSubject_whenRoomNotAllowed() {
        Subject subject = new Subject("OOP", 96, "Alisson", LocalDate.of(2026, 9,15), null, List.of("Sala 114"), List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        Lesson lesson = new Lesson("1", subject);
        lesson.setTimeslot(MONDAY_W1);
        lesson.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomPerSubject)
                .given(lesson)
                .penalizesBy(1);
    }

    @Test
    void roomPerSubject_whenRoomAllowed_noPenalty() {
        Subject subject = new Subject("OOP", 96, "Alisson",LocalDate.of(2026, 9,15), null, List.of("Sala 114"),List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
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
        constraintVerifier.verifyThat(TimetableConstraintProvider::dayOfWeekSubjectConsistency)
                .given(lesson1, lesson2)
                .penalizesBy(8);
    }

    @Test
    void dayOfWeekSubjectConsistency_sameSubject_noPenalty() {
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", SUBJECT_OOP);
        lesson2.setTimeslot(MONDAY_W1);
        lesson2.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::dayOfWeekSubjectConsistency)
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
                .rewardsWith(20);
    }

    @Test
    void weeklyTeacherVariety_sameTeacher_rewardsOne() {
        Week week = new Week(2L);
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", new Subject("OOP2", 48, "Alisson", LocalDate.of(2026, 9,15), null, List.of("Sala 114"),List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)));
        lesson2.setTimeslot(MONDAY_W1);
        lesson2.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weeklyTeacherVariety)
                .given(week, lesson1, lesson2)
                .rewardsWith(10);
    }

    private static Timeslot slotAt(int weekOffset, DayOfWeek dayOfWeek) {
        LocalDate monday = LocalDate.of(2026, 9, 21).plusWeeks(weekOffset);
        return new Timeslot(dayOfWeek + "-" + weekOffset, monday.plusDays(dayOfWeek.getValue() - 1L),
                dayOfWeek, java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
    }

    private static Timeslot mondayOfWeek(int weekOffset) {
        return slotAt(weekOffset, DayOfWeek.MONDAY);
    }

    private static Lesson lessonAt(String id, Subject subject, Timeslot timeslot) {
        Lesson lesson = new Lesson(id, subject);
        lesson.setTimeslot(timeslot);
        lesson.setRoom(ROOM1);
        return lesson;
    }

    @Test
    void teacherThreeDaysInARow_twoConsecutiveDays_noPenalty() {
        Timeslot mon = slotAt(0, DayOfWeek.MONDAY);
        Timeslot tue = slotAt(0, DayOfWeek.TUESDAY);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherThreeDaysInARow)
                .given(lessonAt("1", SUBJECT_OOP, mon), lessonAt("2", SUBJECT_OOP, tue))
                .penalizesBy(0);
    }

    @Test
    void teacherThreeDaysInARow_threeConsecutiveDays_penalizes() {
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherThreeDaysInARow)
                .given(lessonAt("1", SUBJECT_OOP, slotAt(0, DayOfWeek.MONDAY)),
                        lessonAt("2", SUBJECT_OOP, slotAt(0, DayOfWeek.TUESDAY)),
                        lessonAt("3", SUBJECT_OOP, slotAt(0, DayOfWeek.WEDNESDAY)))
                .penalizesBy(60);
    }

    @Test
    void teacherThreeDaysInARow_fourConsecutiveDays_penalizesTwice() {
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherThreeDaysInARow)
                .given(lessonAt("1", SUBJECT_OOP, slotAt(0, DayOfWeek.MONDAY)),
                        lessonAt("2", SUBJECT_OOP, slotAt(0, DayOfWeek.TUESDAY)),
                        lessonAt("3", SUBJECT_OOP, slotAt(0, DayOfWeek.WEDNESDAY)),
                        lessonAt("4", SUBJECT_OOP, slotAt(0, DayOfWeek.THURSDAY)))
                .penalizesBy(120);
    }

    @Test
    void teacherThreeDaysInARow_countsAcrossDifferentSubjectsOfTheSameTeacher() {
        Subject alissonOther = new Subject("BD", 72, "Alisson", LocalDate.of(2026, 9, 15), null,
                List.of("Sala 114"), List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherThreeDaysInARow)
                .given(lessonAt("1", SUBJECT_OOP, slotAt(0, DayOfWeek.MONDAY)),
                        lessonAt("2", alissonOther, slotAt(0, DayOfWeek.TUESDAY)),
                        lessonAt("3", SUBJECT_OOP, slotAt(0, DayOfWeek.WEDNESDAY)))
                .penalizesBy(60);
    }

    @Test
    void teacherThreeDaysInARow_fridayThenMondayIsNotARun() {
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherThreeDaysInARow)
                .given(lessonAt("1", SUBJECT_OOP, slotAt(0, DayOfWeek.THURSDAY)),
                        lessonAt("2", SUBJECT_OOP, slotAt(0, DayOfWeek.FRIDAY)),
                        lessonAt("3", SUBJECT_OOP, slotAt(1, DayOfWeek.MONDAY)))
                .penalizesBy(0);
    }

    @Test
    void teacherThreeDaysInARow_differentTeachers_noPenalty() {
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherThreeDaysInARow)
                .given(lessonAt("1", SUBJECT_OOP, slotAt(0, DayOfWeek.MONDAY)),
                        lessonAt("2", SUBJECT_ALG, slotAt(0, DayOfWeek.TUESDAY)),
                        lessonAt("3", SUBJECT_OOP, slotAt(0, DayOfWeek.WEDNESDAY)))
                .penalizesBy(0);
    }

    @Test
    void stableWeekdayPattern_sameWeekdayEveryWeek_noPenalty() {
        Timeslot w0 = mondayOfWeek(0);
        Timeslot w1 = mondayOfWeek(1);
        Timeslot w2 = mondayOfWeek(2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::stableWeekdayPattern)
                .given(SUBJECT_OOP, w0, w1, w2, lessonAt("1", SUBJECT_OOP, w0),
                        lessonAt("2", SUBJECT_OOP, w1), lessonAt("3", SUBJECT_OOP, w2))
                .penalizesBy(0);
    }

    @Test
    void stableWeekdayPattern_hoppingBetweenWeekdays_penalizes() {
        Timeslot mon0 = slotAt(0, DayOfWeek.MONDAY);
        Timeslot tue1 = slotAt(1, DayOfWeek.TUESDAY);
        Timeslot mon2 = slotAt(2, DayOfWeek.MONDAY);
        constraintVerifier.verifyThat(TimetableConstraintProvider::stableWeekdayPattern)
                .given(SUBJECT_OOP, mon0, slotAt(1, DayOfWeek.MONDAY), mon2,
                        slotAt(0, DayOfWeek.TUESDAY), tue1, slotAt(2, DayOfWeek.TUESDAY),
                        lessonAt("1", SUBJECT_OOP, mon0), lessonAt("2", SUBJECT_OOP, tue1),
                        lessonAt("3", SUBJECT_OOP, mon2))
                .penalizesBy(25);
    }

    @Test
    void stableWeekdayPattern_twoWeekdaysHeldEveryWeek_noPenalty() {
        Timeslot mon0 = slotAt(0, DayOfWeek.MONDAY);
        Timeslot thu0 = slotAt(0, DayOfWeek.THURSDAY);
        Timeslot mon1 = slotAt(1, DayOfWeek.MONDAY);
        Timeslot thu1 = slotAt(1, DayOfWeek.THURSDAY);
        constraintVerifier.verifyThat(TimetableConstraintProvider::stableWeekdayPattern)
                .given(SUBJECT_OOP, mon0, thu0, mon1, thu1,
                        lessonAt("1", SUBJECT_OOP, mon0), lessonAt("2", SUBJECT_OOP, thu0),
                        lessonAt("3", SUBJECT_OOP, mon1), lessonAt("4", SUBJECT_OOP, thu1))
                .penalizesBy(0);
    }

    @Test
    void stableWeekdayPattern_handingAWeekdayOverOnce_noPenalty() {
        Timeslot mon0 = slotAt(0, DayOfWeek.MONDAY);
        Timeslot mon1 = slotAt(1, DayOfWeek.MONDAY);
        Timeslot fri2 = slotAt(2, DayOfWeek.FRIDAY);
        Timeslot fri3 = slotAt(3, DayOfWeek.FRIDAY);
        constraintVerifier.verifyThat(TimetableConstraintProvider::stableWeekdayPattern)
                .given(SUBJECT_OOP, mon0, mon1, slotAt(2, DayOfWeek.MONDAY), slotAt(3, DayOfWeek.MONDAY),
                        slotAt(0, DayOfWeek.FRIDAY), slotAt(1, DayOfWeek.FRIDAY), fri2, fri3,
                        lessonAt("1", SUBJECT_OOP, mon0), lessonAt("2", SUBJECT_OOP, mon1),
                        lessonAt("3", SUBJECT_OOP, fri2), lessonAt("4", SUBJECT_OOP, fri3))
                .penalizesBy(0);
    }

    @Test
    void stableWeekdayPattern_longerHopCostsPerMissingWeek() {
        Timeslot mon0 = slotAt(0, DayOfWeek.MONDAY);
        Timeslot mon3 = slotAt(3, DayOfWeek.MONDAY);
        constraintVerifier.verifyThat(TimetableConstraintProvider::stableWeekdayPattern)
                .given(SUBJECT_OOP, mon0, slotAt(1, DayOfWeek.MONDAY), slotAt(2, DayOfWeek.MONDAY), mon3,
                        lessonAt("1", SUBJECT_OOP, mon0), lessonAt("2", SUBJECT_OOP, mon3))
                .penalizesBy(50);
    }

    @Test
    void weeklySubjectContinuity_consecutiveWeeks_noPenalty() {
        Timeslot w0 = mondayOfWeek(0);
        Timeslot w1 = mondayOfWeek(1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weeklySubjectContinuity)
                .given(SUBJECT_OOP, w0, w1, lessonAt("1", SUBJECT_OOP, w0), lessonAt("2", SUBJECT_OOP, w1))
                .penalizesBy(0);
    }

    @Test
    void weeklySubjectContinuity_oneOffWeekdayShift_keepsRunIntact() {
        Timeslot w0 = mondayOfWeek(0);
        Timeslot w1Tuesday = new Timeslot("t1", LocalDate.of(2026, 9, 29), DayOfWeek.TUESDAY,
                java.time.LocalTime.of(18, 40), java.time.LocalTime.of(22, 0));
        constraintVerifier.verifyThat(TimetableConstraintProvider::weeklySubjectContinuity)
                .given(SUBJECT_OOP, w0, w1Tuesday,
                        lessonAt("1", SUBJECT_OOP, w0), lessonAt("2", SUBJECT_OOP, w1Tuesday))
                .penalizesBy(0);
    }

    @Test
    void weeklySubjectContinuity_oneSkippedWeek_penalizesOnce() {
        Timeslot w0 = mondayOfWeek(0);
        Timeslot w1 = mondayOfWeek(1);
        Timeslot w2 = mondayOfWeek(2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weeklySubjectContinuity)
                .given(SUBJECT_OOP, w0, w1, w2, lessonAt("1", SUBJECT_OOP, w0), lessonAt("2", SUBJECT_OOP, w2))
                .penalizesBy(25);
    }

    @Test
    void weeklySubjectContinuity_hiatusCostsPerMissingWeek() {
        Timeslot w0 = mondayOfWeek(0);
        Timeslot w4 = mondayOfWeek(4);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weeklySubjectContinuity)
                .given(SUBJECT_OOP, w0, mondayOfWeek(1), mondayOfWeek(2), mondayOfWeek(3), w4,
                        lessonAt("1", SUBJECT_OOP, w0), lessonAt("2", SUBJECT_OOP, w4))
                .penalizesBy(75);
    }

    @Test
    void weeklySubjectContinuity_weeksAfterTheCourseEnds_notPenalized() {
        Timeslot w0 = mondayOfWeek(0);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weeklySubjectContinuity)
                .given(SUBJECT_OOP, w0, mondayOfWeek(1), mondayOfWeek(2), lessonAt("1", SUBJECT_OOP, w0))
                .penalizesBy(0);
    }

    @Test
    void weeklySubjectContinuity_weeksBeforeTheCourseStarts_notPenalized() {
        Timeslot w2 = mondayOfWeek(2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weeklySubjectContinuity)
                .given(SUBJECT_OOP, mondayOfWeek(0), mondayOfWeek(1), w2, lessonAt("1", SUBJECT_OOP, w2))
                .penalizesBy(0);
    }

    @Test
    void weeklySubjectContinuity_otherSubjectDoesNotBridgeTheGap() {
        Timeslot w0 = mondayOfWeek(0);
        Timeslot w1 = mondayOfWeek(1);
        Timeslot w2 = mondayOfWeek(2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::weeklySubjectContinuity)
                .given(SUBJECT_OOP, w0, w1, w2,
                        lessonAt("1", SUBJECT_OOP, w0), lessonAt("2", SUBJECT_ALG, w1),
                        lessonAt("3", SUBJECT_OOP, w2))
                .penalizesBy(25);
    }

    @Test
    void sameWeekdayPerSubject_singleLesson_noPenalty() {
        Lesson lesson = new Lesson("1", SUBJECT_OOP);
        lesson.setTimeslot(MONDAY_W1);
        lesson.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::sameWeekdayPerSubject)
                .given(lesson)
                .penalizesBy(0);
    }

    @Test
    void sameWeekdayPerSubject_allLessonsOnSameWeekday_noPenalty() {
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", SUBJECT_OOP);
        lesson2.setTimeslot(MONDAY_W2);
        lesson2.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::sameWeekdayPerSubject)
                .given(lesson1, lesson2)
                .penalizesBy(0);
    }

    @Test
    void sameWeekdayPerSubject_oneOffShift_penalizesOnceAndResumeIsFree() {
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", SUBJECT_OOP);
        lesson2.setTimeslot(TUESDAY_W1);
        lesson2.setRoom(ROOM1);
        Lesson lesson3 = new Lesson("3", SUBJECT_OOP);
        lesson3.setTimeslot(MONDAY_W2);
        lesson3.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::sameWeekdayPerSubject)
                .given(lesson1, lesson2, lesson3)
                .penalizesBy(25);
    }

    @Test
    void sameWeekdayPerSubject_regularTwiceAWeekCostsOneExtraWeekday() {
        Lesson mon1 = new Lesson("1", SUBJECT_OOP);
        mon1.setTimeslot(MONDAY_W1);
        mon1.setRoom(ROOM1);
        Lesson tue1 = new Lesson("2", SUBJECT_OOP);
        tue1.setTimeslot(TUESDAY_W1);
        tue1.setRoom(ROOM1);
        Lesson mon2 = new Lesson("3", SUBJECT_OOP);
        mon2.setTimeslot(MONDAY_W2);
        mon2.setRoom(ROOM1);
        Lesson tue2 = new Lesson("4", SUBJECT_OOP);
        tue2.setTimeslot(TUESDAY_W2);
        tue2.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::sameWeekdayPerSubject)
                .given(mon1, tue1, mon2, tue2)
                .penalizesBy(25);
    }

    @Test
    void sameWeekdayPerSubject_eachAdditionalWeekdayCostsOneUnit() {
        Lesson lesson1 = new Lesson("1", SUBJECT_OOP);
        lesson1.setTimeslot(MONDAY_W1);
        lesson1.setRoom(ROOM1);
        Lesson lesson2 = new Lesson("2", SUBJECT_OOP);
        lesson2.setTimeslot(TUESDAY_W1);
        lesson2.setRoom(ROOM1);
        Lesson lesson3 = new Lesson("3", SUBJECT_OOP);
        lesson3.setTimeslot(SATURDAY);
        lesson3.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::sameWeekdayPerSubject)
                .given(lesson1, lesson2, lesson3)
                .penalizesBy(100);
    }

    @Test
    void sameWeekdayPerSubject_separateSubjectsScoredIndependently() {
        Lesson oop = new Lesson("1", SUBJECT_OOP);
        oop.setTimeslot(MONDAY_W1);
        oop.setRoom(ROOM1);
        Lesson alg = new Lesson("2", SUBJECT_ALG);
        alg.setTimeslot(TUESDAY_W1);
        alg.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::sameWeekdayPerSubject)
                .given(oop, alg)
                .penalizesBy(0);
    }

    @Test
    void idleClassDay_gapBeforeRemainingLesson_penalizesOnce() {
        Lesson later = new Lesson("1", SUBJECT_OOP);
        later.setTimeslot(TUESDAY_IN_WINDOW);
        later.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::idleClassDay)
                .given(ROOM1, MONDAY_IN_WINDOW, TUESDAY_IN_WINDOW, SUBJECT_OOP, later)
                .penalizesBy(30);
    }

    @Test
    void idleClassDay_chargeIsFlatRegardlessOfHowManyLessonsFollow() {
        Lesson later1 = new Lesson("1", SUBJECT_OOP);
        later1.setTimeslot(TUESDAY_IN_WINDOW);
        later1.setRoom(ROOM1);
        Lesson later2 = new Lesson("2", SUBJECT_ALG);
        later2.setTimeslot(WEDNESDAY_IN_WINDOW);
        later2.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::idleClassDay)
                .given(ROOM1, MONDAY_IN_WINDOW, TUESDAY_IN_WINDOW, WEDNESDAY_IN_WINDOW, SUBJECT_OOP, later1, later2)
                .penalizesBy(30);
    }

    @Test
    void idleClassDay_trailingIdleDayAfterAllLessons_noPenalty() {
        Lesson earlier = new Lesson("1", SUBJECT_OOP);
        earlier.setTimeslot(MONDAY_IN_WINDOW);
        earlier.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::idleClassDay)
                .given(ROOM1, MONDAY_IN_WINDOW, TUESDAY_IN_WINDOW, SUBJECT_OOP, earlier)
                .penalizesBy(0);
    }

    @Test
    void idleClassDay_noLessonsAtAll_noPenalty() {
        constraintVerifier.verifyThat(TimetableConstraintProvider::idleClassDay)
                .given(ROOM1, MONDAY_IN_WINDOW, SUBJECT_OOP)
                .penalizesBy(0);
    }

    @Test
    void idleClassDay_laterLessonInAnotherRoom_noPenalty() {
        Lesson later = new Lesson("1", SUBJECT_OOP);
        later.setTimeslot(TUESDAY_IN_WINDOW);
        later.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::idleClassDay)
                .given(ROOM1, MONDAY_IN_WINDOW, TUESDAY_IN_WINDOW, SUBJECT_OOP, later)
                .penalizesBy(0);
    }

    @Test
    void idleClassDay_dayBeforeSubjectStart_noPenalty() {
        Lesson later = new Lesson("1", SUBJECT_OOP);
        later.setTimeslot(TUESDAY_IN_WINDOW);
        later.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::idleClassDay)
                .given(ROOM1, MONDAY_BEFORE_WINDOW, TUESDAY_IN_WINDOW, SUBJECT_OOP, later)
                .penalizesBy(0);
    }

    @Test
    void idleClassDay_weekdayNotDesignedForSubject_noPenalty() {
        Subject tuesdayOnly = new Subject("Redes", 40, "Nelma", LocalDate.of(2026, 9, 15), null,
                List.of("Sala 114"), List.of(DayOfWeek.TUESDAY));
        Lesson later = new Lesson("1", tuesdayOnly);
        later.setTimeslot(TUESDAY_IN_WINDOW);
        later.setRoom(ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::idleClassDay)
                .given(ROOM1, MONDAY_IN_WINDOW, TUESDAY_IN_WINDOW, tuesdayOnly, later)
                .penalizesBy(0);
    }

    @Test
    void idleClassDay_subjectDesignedForAnotherRoom_noPenalty() {
        Lesson later = new Lesson("1", SUBJECT_OOP);
        later.setTimeslot(TUESDAY_IN_WINDOW);
        later.setRoom(ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::idleClassDay)
                .given(ROOM2, MONDAY_IN_WINDOW, TUESDAY_IN_WINDOW, SUBJECT_OOP, later)
                .penalizesBy(0);
    }
}