package org.acme.schooltimetabling.solver;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Week;
import org.acme.schooltimetabling.solver.justifications.RoomConflictJustification;
import org.acme.schooltimetabling.solver.justifications.TeacherConflictJustification;
import org.jspecify.annotations.NonNull;

import java.time.DayOfWeek;
import java.util.List;

public class TimetableConstraintProvider implements ConstraintProvider {

    private static final List<DayOfWeek> VALID_DAYS = List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory factory) {
        return new Constraint[] {
                // HARD
                roomConflict(factory),
                teacherConflict(factory),
                dayOfWeekSubjectConsistency(factory),
                weeklyTeacherVariety(factory),
                consecutiveWeeksSameWeekday(factory),
                roomPerSubject(factory),
                classesOnlyInBetweenSubjectDates(factory),

                fullWeekCoverage(factory),
                daysWithoutClass(factory),

                // SOFT
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
                        .reward(HardSoftScore.ONE_SOFT, (week, teacherCount) -> teacherCount)
                        .asConstraint("Weekly teacher variety");
                }

        Constraint daysWithoutClass(ConstraintFactory factory){
                return factory.forEach(Lesson.class)
                .filter(lesson -> !lesson.getSubject().getDesignDayOfWeeks().contains(lesson.getTimeslot().getDayOfWeek()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Only days which should have class are allowed");
        }

        Constraint fullWeekCoverage(ConstraintFactory factory) {
        return factory.forEach(Week.class)
                .join(Lesson.class,
                        Joiners.equal(Week::getWeekOfYear, lesson -> lesson.getTimeslot().getWeekOfYear()))
                .groupBy((week, lesson) -> week,
                        ConstraintCollectors.countDistinct((week, lesson) -> lesson.getTimeslot().getDayOfWeek()))
                .penalize(HardSoftScore.ONE_SOFT, (week, dayCount) -> ((52 - week.getWeekOfYear()) / 2) * Math.abs(VALID_DAYS.size() - dayCount))
                .asConstraint("Full week coverage");
        }

        Constraint consecutiveWeeksSameWeekday(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .ifNotExists(
                        Lesson.class,
                        Joiners.equal(Lesson::getSubject),
                        Joiners.equal(
                                lesson -> lesson.getTimeslot().getWeekOfYear() + 1,
                                lesson -> lesson.getTimeslot().getWeekOfYear()
                        ),
                        Joiners.equal(
                                lesson -> lesson.getTimeslot().getDayOfWeek(),
                                lesson -> lesson.getTimeslot().getDayOfWeek()
                        )
                )
                .penalize(HardSoftScore.ONE_SOFT, (lesson) -> 25)
                .asConstraint("Subject should be on the same weekday in consecutive weeks");
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
    // -------------------------
    // SOFT CONSTRAINTS
    // -------------------------

    

}