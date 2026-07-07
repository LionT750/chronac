package org.acme.schooltimetabling.solver;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.solver.justifications.RoomConflictJustification;
import org.jspecify.annotations.NonNull;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;

public class TimetableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory factory) {
        return new Constraint[] {
                // HARD
                roomConflict(factory),
                backToBackLessons(factory),
                teacherCantDay(factory, "Rodolfo", DayOfWeek.FRIDAY),
                
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

    Constraint backToBackLessons(ConstraintFactory factory) {
        // Each lesson should be spread evenly across the weeks of the semester.
        return factory.forEachUniquePair(Lesson.class,
        Joiners.equal(Lesson::getTeacher))
        .filter((l1, l2) -> {
                long dayDiff = Math.abs(ChronoUnit.DAYS.between(
                        l1.getTimeslot().getDate(),
                        l2.getTimeslot().getDate()
                ));
                return dayDiff == 1;
        })
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Back to back lessons");
        }

      Constraint teacherCantDay(ConstraintFactory factory, String teacher, DayOfWeek dayOfWeek) {
        // Each lesson should be spread evenly across the weeks of the semester.
        return factory.forEach(Lesson.class)
        .filter(lesson -> teacher.equals(lesson.getTeacher()) && dayOfWeek.equals(lesson.getTimeslot().getDayOfWeek()))
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Teacher " + teacher + " can't have lessons on " + dayOfWeek);
        }
    // -------------------------
    // SOFT CONSTRAINTS
    // -------------------------

    

}