package br.com.chronac.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
import br.com.chronac.domain.Lesson;

import java.time.LocalDate;

public record TeacherDateUnavailableJustification(String teacher, LocalDate date, Lesson lesson, String description)
        implements
            ConstraintJustification {

    public TeacherDateUnavailableJustification(String teacher, LocalDate date, Lesson lesson) {
        this(teacher, date, lesson,
                "Teacher '%s' is unavailable on %s but lesson '%s' is scheduled"
                        .formatted(teacher, date, lesson.getSubject().getName()));
    }
}