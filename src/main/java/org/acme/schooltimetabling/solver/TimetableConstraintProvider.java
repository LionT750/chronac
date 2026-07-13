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
import org.jspecify.annotations.NonNull;

import java.time.DayOfWeek;
import java.util.List;

public class TimetableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory factory) {
        List<DayOfWeek> days = List.of(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
        );
        return new Constraint[] {
                // HARD
                roomConflict(factory),
                dayOfWeekTeacherConsistency(factory),
                weeklyTeacherVariety(factory),
                consecutiveWeeksSameWeekday(factory),
                roomPerSubject(factory),

                fullWeekCoverage(factory,days),
                daysWithoutClass(factory,days)
                
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

        Constraint roomPerSubject(ConstraintFactory factory)
        {
                return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getSubject().getDesignedRooms().contains(lesson.getRoom().getName()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Cant have class in not allowed rooms");
        }
    
        Constraint dayOfWeekTeacherConsistency(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .groupBy(
                        lesson -> lesson.getTimeslot().getDayOfWeek(),
                        ConstraintCollectors.countDistinct(Lesson::getTeacher)
                )
                .penalize(HardSoftScore.ONE_SOFT,
                        (dayOfWeek, teacherCount) -> (teacherCount - 1) * 5)
                .asConstraint("Consistent teacher per weekday slot");
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

        Constraint daysWithoutClass(ConstraintFactory factory, List<DayOfWeek> validDaysOfWeek){
                return factory.forEach(Lesson.class)
                .filter(lesson -> !validDaysOfWeek.contains(lesson.getTimeslot().getDayOfWeek()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Only days which should have class are allowed");
        }

        Constraint fullWeekCoverage(ConstraintFactory factory, List<DayOfWeek> validDaysOfWeek) {
        return factory.forEach(Week.class)
                .join(Lesson.class,
                        Joiners.equal(Week::getWeekOfYear, lesson -> lesson.getTimeslot().getWeekOfYear()))
                .groupBy((week, lesson) -> week,
                        ConstraintCollectors.countDistinct((week, lesson) -> lesson.getTimeslot().getDayOfWeek()))
                .penalize(HardSoftScore.ONE_SOFT, (week, dayCount) -> ((52 - week.getWeekOfYear()) / 2) * Math.abs(validDaysOfWeek.size() - dayCount))
                .asConstraint("Full week coverage");
        }

        Constraint consecutiveWeeksSameWeekday(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .ifNotExists(
                        Lesson.class,
                        Joiners.equal(Lesson::getTeacher),
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
                .asConstraint("Teacher should teach on the same weekday in consecutive weeks");
        }
    // -------------------------
    // SOFT CONSTRAINTS
    // -------------------------

    

}