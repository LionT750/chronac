package br.com.chronac.solver;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import org.jspecify.annotations.NonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import br.com.chronac.domain.Lesson;
import br.com.chronac.domain.Room;
import br.com.chronac.domain.Subject;
import br.com.chronac.domain.TeacherSchedule;
import br.com.chronac.domain.Timeslot;
import br.com.chronac.domain.Week;
import br.com.chronac.solver.justifications.RoomConflictJustification;
import br.com.chronac.solver.justifications.TeacherConflictJustification;
import br.com.chronac.solver.justifications.TeacherDateUnavailableJustification;
import br.com.chronac.solver.justifications.TeacherWeeklyQuotaJustification;

public class TimetableConstraintProvider implements ConstraintProvider {

    private static final int IDLE_CLASS_DAY_PENALTY = 30;

    private static final int EXTRA_WEEKDAY_PENALTY = 25;

    private static final int WEEK_GAP_PENALTY = 25;

    private static final int WEEKDAY_PATTERN_PENALTY = 25;

    private static final int TEACHER_THREE_IN_A_ROW_PENALTY = 60;

    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory factory) {
        return new Constraint[] {
                // HARD
                roomConflict(factory),
                teacherConflict(factory),
                dayOfWeekSubjectConsistency(factory),
                weeklyTeacherVariety(factory),
                sameWeekdayPerSubject(factory),
                weeklySubjectContinuity(factory),
                stableWeekdayPattern(factory),
                teacherThreeDaysInARow(factory),
                roomPerSubject(factory),
                classesOnlyInBetweenSubjectDates(factory),
                teacherDateUnavailability(factory),

                daysWithoutClass(factory),
                idleClassDay(factory),
                compactSchedule(factory),

        };
    }

    // -------------------------
    // HARD CONSTRAINTS
    // -------------------------

     Constraint roomConflict(ConstraintFactory constraintFactory) {
        // A room can accommodate at most one lesson at the same time.
        return constraintFactory
                // Select each pair of 2 different lessons ...
                .forEachUniquePair(Lesson.class,
                        // ... in the same timeslot ...
                        Joiners.equal(Lesson::getTimeslot),
                        // ... in the same room ...
                        Joiners.equal(Lesson::getRoom))
                // ... and penalize each pair with a hard weight.
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith((lesson1, lesson2, score) -> new RoomConflictJustification(lesson1.getRoom(), lesson1, lesson2))
                .asConstraint("Room conflict");
    }

        Constraint teacherConflict(ConstraintFactory constraintFactory) {
        // A room can accommodate at most one lesson at the same time.
        return constraintFactory
                // Select each pair of 2 different lessons ...
                .forEachUniquePair(Lesson.class,
                        // ... in the same timeslot ...
                        Joiners.equal(Lesson::getTimeslot),
                        // ... in the same room ...
                        Joiners.equal(Lesson::getTeacher))
                // ... and penalize each pair with a hard weight.
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith((lesson1, lesson2, score) -> new TeacherConflictJustification(lesson1.getTeacher(), lesson1, lesson2))
                .asConstraint("Teacher conflict");
        }

        Constraint roomPerSubject(ConstraintFactory factory)
        {
                return factory.forEach(Lesson.class)
                .filter(lesson -> !lesson.getSubject().getDesignedRooms().contains(lesson.getRoom().getName()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Cant have class in not allowed rooms");
        }

        Constraint dayOfWeekSubjectConsistency(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .groupBy(
                        lesson -> lesson.getTimeslot().getDayOfWeek(),
                        ConstraintCollectors.countDistinct(Lesson::getSubject)
                )
                .penalize(HardSoftScore.ONE_SOFT,
                        (dayOfWeek, subjectCount) -> (subjectCount - 1) * 8)
                .asConstraint("Consistent subject per weekday slot");
        }

        Constraint weeklyTeacherVariety(ConstraintFactory factory) {
                return factory.forEach(Week.class)
                        .join(Lesson.class,
                                Joiners.equal(Week::getWeekOfYear, lesson -> lesson.getTimeslot().getWeekOfYear()))
                        .groupBy((week, lesson) -> week,
                                ConstraintCollectors.countDistinct((week, lesson) -> lesson.getTeacher()))
                        .reward(HardSoftScore.ONE_SOFT, (week, teacherCount) -> teacherCount * 10)
                        .asConstraint("Weekly teacher variety");
                }

        Constraint daysWithoutClass(ConstraintFactory factory){
                return factory.forEach(Lesson.class)
                .filter(lesson -> !lesson.getSubject().getEffectiveDayOfWeeks().contains(lesson.getTimeslot().getDayOfWeek()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Only days which should have class are allowed");
        }

        Constraint idleClassDay(ConstraintFactory factory) {
                return factory.forEach(Room.class)
                        .join(Timeslot.class)
                        .ifExists(Subject.class,
                                Joiners.filtering((room, timeslot, subject) ->
                                        subject.getDesignedRooms().contains(room.getName())
                                                && subject.canHaveClassOn(timeslot.getDate())))
                        .ifNotExists(Lesson.class,
                                Joiners.equal((room, timeslot) -> room, Lesson::getRoom),
                                Joiners.equal((room, timeslot) -> timeslot, Lesson::getTimeslot))
                        .ifExists(Lesson.class,
                                Joiners.equal((room, timeslot) -> room, Lesson::getRoom),
                                Joiners.lessThan((room, timeslot) -> timeslot.getDate(),
                                        lesson -> lesson.getTimeslot().getDate()))
                        .penalize(HardSoftScore.ONE_SOFT,
                                (room, timeslot) -> IDLE_CLASS_DAY_PENALTY)
                        .asConstraint("Idle day before remaining lessons");
        }

        Constraint teacherThreeDaysInARow(ConstraintFactory factory) {
                return factory.forEach(Lesson.class)
                        .join(Lesson.class,
                                Joiners.equal(Lesson::getTeacher),
                                Joiners.equal(lesson -> lesson.getTimeslot().getDate().plusDays(1),
                                        lesson -> lesson.getTimeslot().getDate()))
                        .join(Lesson.class,
                                Joiners.equal((first, second) -> first.getTeacher(), Lesson::getTeacher),
                                Joiners.equal((first, second) -> first.getTimeslot().getDate().plusDays(2),
                                        lesson -> lesson.getTimeslot().getDate()))
                        .penalize(HardSoftScore.ONE_SOFT,
                                (first, second, third) -> TEACHER_THREE_IN_A_ROW_PENALTY)
                        .justifyWith((first, second, third, score) -> new TeacherWeeklyQuotaJustification(
                                first.getTeacher(), first.getTimeslot().getWeekStart(), 3L, 2))
                        .asConstraint("Teacher should not teach three days in a row");
        }

        Constraint stableWeekdayPattern(ConstraintFactory factory) {
                return factory.forEach(Subject.class)
                        .join(Timeslot.class,
                                Joiners.filtering((subject, timeslot) ->
                                        subject.canHaveClassOn(timeslot.getDate())))
                        .groupBy((subject, timeslot) -> subject,
                                (subject, timeslot) -> timeslot.getDayOfWeek(),
                                (subject, timeslot) -> timeslot.getWeekStart())
                        .ifNotExists(Lesson.class,
                                Joiners.equal((Subject subject, DayOfWeek dayOfWeek, LocalDate weekStart) -> subject,
                                        Lesson::getSubject),
                                Joiners.equal((Subject subject, DayOfWeek dayOfWeek, LocalDate weekStart) -> dayOfWeek,
                                        lesson -> lesson.getTimeslot().getDayOfWeek()),
                                Joiners.equal((Subject subject, DayOfWeek dayOfWeek, LocalDate weekStart) -> weekStart,
                                        lesson -> lesson.getTimeslot().getWeekStart()))
                        .ifExists(Lesson.class,
                                Joiners.equal((Subject subject, DayOfWeek dayOfWeek, LocalDate weekStart) -> subject,
                                        Lesson::getSubject),
                                Joiners.equal((Subject subject, DayOfWeek dayOfWeek, LocalDate weekStart) -> dayOfWeek,
                                        lesson -> lesson.getTimeslot().getDayOfWeek()),
                                Joiners.greaterThan((Subject subject, DayOfWeek dayOfWeek, LocalDate weekStart) -> weekStart,
                                        lesson -> lesson.getTimeslot().getWeekStart()))
                        .ifExists(Lesson.class,
                                Joiners.equal((Subject subject, DayOfWeek dayOfWeek, LocalDate weekStart) -> subject,
                                        Lesson::getSubject),
                                Joiners.equal((Subject subject, DayOfWeek dayOfWeek, LocalDate weekStart) -> dayOfWeek,
                                        lesson -> lesson.getTimeslot().getDayOfWeek()),
                                Joiners.lessThan((Subject subject, DayOfWeek dayOfWeek, LocalDate weekStart) -> weekStart,
                                        lesson -> lesson.getTimeslot().getWeekStart()))
                        .penalize(HardSoftScore.ONE_SOFT,
                                (subject, dayOfWeek, weekStart) -> WEEKDAY_PATTERN_PENALTY)
                        .asConstraint("Subject should keep the same weekday slot every week");
        }

        Constraint weeklySubjectContinuity(ConstraintFactory factory) {
                return factory.forEach(Subject.class)
                        .join(Timeslot.class,
                                Joiners.filtering((subject, timeslot) ->
                                        subject.canHaveClassOn(timeslot.getDate())))
                        .groupBy((subject, timeslot) -> subject,
                                (subject, timeslot) -> timeslot.getWeekStart())
                        .ifNotExists(Lesson.class,
                                Joiners.equal((subject, weekStart) -> subject, Lesson::getSubject),
                                Joiners.equal((subject, weekStart) -> weekStart,
                                        lesson -> lesson.getTimeslot().getWeekStart()))
                        .ifExists(Lesson.class,
                                Joiners.equal((subject, weekStart) -> subject, Lesson::getSubject),
                                Joiners.greaterThan((subject, weekStart) -> weekStart,
                                        lesson -> lesson.getTimeslot().getWeekStart()))
                        .ifExists(Lesson.class,
                                Joiners.equal((subject, weekStart) -> subject, Lesson::getSubject),
                                Joiners.lessThan((subject, weekStart) -> weekStart,
                                        lesson -> lesson.getTimeslot().getWeekStart()))
                        .penalize(HardSoftScore.ONE_SOFT,
                                (subject, weekStart) -> WEEK_GAP_PENALTY)
                        .asConstraint("Subject should run on consecutive weeks");
        }

        Constraint sameWeekdayPerSubject(ConstraintFactory factory) {
                return factory.forEach(Lesson.class)
                        .groupBy(Lesson::getSubject,
                                ConstraintCollectors.countDistinct(
                                        lesson -> lesson.getTimeslot().getDayOfWeek()))
                        .penalize(HardSoftScore.ONE_SOFT,
                                (subject, distinctWeekdays) -> (distinctWeekdays - 1)
                                        * (distinctWeekdays - 1) * EXTRA_WEEKDAY_PENALTY)
                        .asConstraint("Subject should use as few weekdays as possible");
        }

        Constraint classesOnlyInBetweenSubjectDates(ConstraintFactory factory)
        {
                return factory.forEach(Lesson.class)
                        .filter(lesson -> {
                                        if (lesson.getTimeslot().getDate().isBefore(lesson.getSubject().getStartDate()))
                                                return true;
                                        if (lesson.getSubject().getEndDate() != null)
                                                if (lesson.getTimeslot().getDate().isAfter(lesson.getSubject().getEndDate()))
                                                        return true;
                                        return false;
                                        })
                                        .penalize(HardSoftScore.ONE_HARD)
                        .asConstraint("Classes must happen only after UC start and before it ends");
                }
        Constraint teacherDateUnavailability(ConstraintFactory factory) {
                return factory.forEach(Lesson.class)
                        .join(factory.forEach(TeacherSchedule.class),
                                Joiners.equal(Lesson::getTeacher, TeacherSchedule::getTeacherName))
                        .filter((lesson, schedule) -> !schedule.isDateAvailable(lesson.getTimeslot().getDate()))
                        .penalize(HardSoftScore.ONE_HARD)
                        .justifyWith((lesson, schedule, score) -> new TeacherDateUnavailableJustification(lesson.getTeacher(), lesson.getTimeslot().getDate(), lesson))
                        .asConstraint("Teacher unavailable on date");
        }

        Constraint compactSchedule(ConstraintFactory factory) {
                return factory.forEach(Lesson.class)
                        .groupBy(
                                ConstraintCollectors.min((Lesson lesson) -> lesson.getTimeslot().getDate()),
                                ConstraintCollectors.max((Lesson lesson) -> lesson.getTimeslot().getDate())
                        )
                        .filter((minDate, maxDate) -> minDate != null && maxDate != null)
                        .penalize(HardSoftScore.ONE_SOFT,
                                (minDate, maxDate) -> (int) ChronoUnit.DAYS.between(minDate, maxDate))
                        .asConstraint("Compact schedule");
        }

    // -------------------------
    // SOFT CONSTRAINTS
    // -------------------------



}