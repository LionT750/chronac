package br.com.chronac.solver;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import org.jspecify.annotations.NonNull;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.chronac.domain.Lesson;
import br.com.chronac.domain.Room;
import br.com.chronac.domain.Subject;
import br.com.chronac.domain.TeacherSchedule;
import br.com.chronac.domain.Timeslot;
import br.com.chronac.solver.justifications.RoomConflictJustification;
import br.com.chronac.solver.justifications.TeacherConflictJustification;
import br.com.chronac.solver.justifications.TeacherDateUnavailableJustification;

public class TimetableConstraintProvider implements ConstraintProvider {

    private static final int CADENCE_OVERRUN_WEIGHT = 20;
    private static final int CADENCE_STEADINESS_WEIGHT = 8;
    private static final int WEEKDAY_STABILITY_WEIGHT = 25;
    private static final int ROOM_FILL_WEIGHT = 20;

    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory factory) {
        return new Constraint[] {
                // HARD
                roomConflict(factory),
                teacherConflict(factory),
                roomPerSubject(factory),
                daysWithoutClass(factory),
                classesOnlyInBetweenSubjectDates(factory),
                teacherDateUnavailability(factory),

                // SOFT
                subjectCompaction(factory),
                cadenceOverrun(factory),
                cadenceSteadiness(factory),
                weekdayStability(factory),
                roomFill(factory),
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
        // A teacher can be in at most one lesson at the same time.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        // ... in the same timeslot ...
                        Joiners.equal(Lesson::getTimeslot),
                        // ... with the same teacher ...
                        Joiners.equal(Lesson::getTeacher))
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith((lesson1, lesson2, score) -> new TeacherConflictJustification(lesson1.getTeacher(), lesson1, lesson2))
                .asConstraint("Teacher conflict");
    }

    Constraint roomPerSubject(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> !lesson.getSubject().getDesignedRooms().contains(lesson.getRoom().getName()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Cant have class in not allowed rooms");
    }

    Constraint daysWithoutClass(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> !lesson.getSubject().getEffectiveDayOfWeeks().contains(lesson.getTimeslot().getDayOfWeek()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Only days which should have class are allowed");
    }

    Constraint classesOnlyInBetweenSubjectDates(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> {
                    if (lesson.getTimeslot().getDate().isBefore(lesson.getSubject().getStartDate())) {
                        return true;
                    }
                    if (lesson.getSubject().getEndDate() != null) {
                        if (lesson.getTimeslot().getDate().isAfter(lesson.getSubject().getEndDate())) {
                            return true;
                        }
                    }
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

    // -------------------------
    // SOFT CONSTRAINTS
    // -------------------------

    Constraint subjectCompaction(ConstraintFactory factory) {
        // Pack each subject's lessons as close as possible to its start date,
        // leaving the tail of the semester naturally empty.
        return factory.forEach(Lesson.class)
                .groupBy(
                        Lesson::getSubject,
                        ConstraintCollectors.max((Lesson lesson) -> lesson.getTimeslot().getDate()))
                .filter((subject, lastDate) -> subject.getStartDate() != null
                        && subject.getWeeklyCadenceCap() > 0
                        && lastDate != null)
                .penalize(HardSoftScore.ONE_SOFT,
                        (subject, lastDate) -> (int) Math.max(0,
                                ChronoUnit.DAYS.between(subject.getStartDate(), lastDate)))
                .asConstraint("Subject packed from its start date");
    }

    Constraint cadenceOverrun(ConstraintFactory factory) {
        // Lessons per week beyond the subject's cadence cap cost 20 each.
        // A week may gain an extra day when the workload needs it, at a price.
        return factory.forEach(Lesson.class)
                .groupBy(
                        Lesson::getSubject,
                        (Lesson lesson) -> lesson.getTimeslot().getWeekOfYear(),
                        ConstraintCollectors.count())
                .filter((subject, week, count) -> subject.getWeeklyCadenceCap() > 0
                        && count > subject.getWeeklyCadenceCap())
                .penalize(HardSoftScore.ONE_SOFT,
                        (subject, week, count) -> (count - subject.getWeeklyCadenceCap()) * CADENCE_OVERRUN_WEIGHT)
                .asConstraint("Weekly cadence cap");
    }

    Constraint cadenceSteadiness(ConstraintFactory factory) {
        // Active weeks below the cadence cap break the rhythm; 8 per missing lesson.
        return factory.forEach(Lesson.class)
                .groupBy(
                        Lesson::getSubject,
                        (Lesson lesson) -> lesson.getTimeslot().getWeekOfYear(),
                        ConstraintCollectors.count())
                .filter((subject, week, count) -> subject.getWeeklyCadenceCap() > 0
                        && count > 0
                        && count < subject.getWeeklyCadenceCap())
                .penalize(HardSoftScore.ONE_SOFT,
                        (subject, week, count) -> (subject.getWeeklyCadenceCap() - count) * CADENCE_STEADINESS_WEIGHT)
                .asConstraint("Steady weekly cadence");
    }

    Constraint weekdayStability(ConstraintFactory factory) {
        // Each subject must keep a fixed set of weekdays while it lasts. Its
        // "home" weekdays are the most-used weeklyCadenceCap weekdays; every
        // lesson landing on another weekday breaks the rhythm and is penalized.
        // Working per subject (not per adjacent week) makes it tolerant of
        // skipped weeks and gives the solver a per-move gradient to consolidate.
        return factory.forEach(Lesson.class)
                .groupBy(
                        Lesson::getSubject,
                        ConstraintCollectors.toList(
                                (Lesson lesson) -> lesson.getTimeslot().getDayOfWeek()))
                .filter((subject, weekdays) -> subject.getWeeklyCadenceCap() > 0)
                .penalize(HardSoftScore.ONE_SOFT,
                        (subject, weekdays) -> lessonsOffHomeWeekdays(subject.getWeeklyCadenceCap(), weekdays)
                                * WEEKDAY_STABILITY_WEIGHT)
                .asConstraint("Subject keeps its weekday from week to week");
    }

    private static long lessonsOffHomeWeekdays(long cap, List<DayOfWeek> weekdays) {
        Map<DayOfWeek, Long> counts = weekdays.stream()
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));
        List<DayOfWeek> home = counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<DayOfWeek, Long>>comparingLong(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(cap)
                .map(Map.Entry::getKey)
                .toList();
        return weekdays.stream()
                .filter(day -> !home.contains(day))
                .count();
    }

    Constraint roomFill(ConstraintFactory factory) {
        // Penalize empty (timeslot, room) pairs that sit inside the room's active
        // span: the weekday was already used earlier (ramp passed) and the room
        // still has classes in a later week. Ramp-up and the final taper stay free,
        // but a weekday that drops out mid-semester while the room stays busy on
        // other days is exactly the hole the user wants eliminated.
        return factory.forEach(Timeslot.class)
                .join(factory.forEach(Room.class))
                .ifExists(Lesson.class,
                        Joiners.equal((Timeslot timeslot, Room room) -> room, Lesson::getRoom),
                        Joiners.equal((Timeslot timeslot, Room room) -> timeslot.getDayOfWeek(),
                                (Lesson lesson) -> lesson.getTimeslot().getDayOfWeek()),
                        Joiners.lessThan((Timeslot timeslot, Room room) -> timeslot.getDate(),
                                (Lesson lesson) -> lesson.getTimeslot().getDate()))
                .ifExists(Lesson.class,
                        Joiners.equal((Timeslot timeslot, Room room) -> room, Lesson::getRoom),
                        Joiners.greaterThan((Timeslot timeslot, Room room) -> timeslot.getDate(),
                                (Lesson lesson) -> lesson.getTimeslot().getDate()))
                .ifNotExists(Lesson.class,
                        Joiners.equal((Timeslot timeslot, Room room) -> timeslot, Lesson::getTimeslot),
                        Joiners.equal((Timeslot timeslot, Room room) -> room, Lesson::getRoom))
                .penalize(HardSoftScore.ONE_SOFT, (Timeslot timeslot, Room room) -> ROOM_FILL_WEIGHT)
                .asConstraint("Room has no mid-semester holes");
    }
}