package org.acme.schooltimetabling.solver;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

import ai.timefold.solver.core.api.score.stream.Joiners;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Week;
import org.acme.schooltimetabling.solver.justifications.RoomConflictJustification;
import org.jspecify.annotations.NonNull;

public class TimetableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory factory) {
        return new Constraint[] {
                // HARD
                roomConflict(factory),
                teacherSpread(factory),
                teacherDays(factory),
                emptyWeek(factory)
                
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



      Constraint noMoreEmptyLessons(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
        .filter((lesson) -> lesson.getTimeslot() == null)
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("No empty lessons");
        }

        Constraint teacherDays(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .groupBy(
                        Lesson::getTeacher,
                        ConstraintCollectors.toList()
                )
                .reward(HardSoftScore.ONE_SOFT,
                        (teacher, lessons) -> {
                                Set<DayOfWeek> days = new HashSet<>();

                                for (Lesson lesson : lessons) {
                                        days.add(lesson.getTimeslot().getDayOfWeek());
                                }

                                if (days.size() <= 2)
                                        return 10;
                                else
                                        return 0;
                        }
                )
                .asConstraint("teacherDays");
}

        Constraint teacherSpread(ConstraintFactory factory) {
                return factory.forEach(Week.class)
                        .join(
                                Lesson.class,
                                Joiners.equal(
                                        Week::getWeekOfYear,
                                        lesson -> lesson.getTimeslot().getWeekOfYear()
                                )
                        )
                        .groupBy(
                                (week, lesson) -> week,
                                ConstraintCollectors.countDistinct((week, lesson) -> lesson.getTeacher())
                        )
                        .filter((week, teacherCount) -> teacherCount < 4)
                        .penalize(HardSoftScore.ONE_HARD)
                        .asConstraint("Every week should have lessons from all teachers");
                }


        Constraint emptyWeek(ConstraintFactory factory) {
        return factory.forEach(Week.class)
                .ifNotExists(
                        Lesson.class,
                        Joiners.equal(
                                Week::getWeekOfYear,
                                lesson -> lesson.getTimeslot().getWeekOfYear()
                        )
                )
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("No empty weeks");
        }
    // -------------------------
    // SOFT CONSTRAINTS
    // -------------------------

    

}