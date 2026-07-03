package org.acme.schooltimetabling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import java.time.LocalDate;

public record TeacherWeeklyQuotaJustification(
        String teacher,
        LocalDate weekStart,
        Long lessonCount,
        int maxLessons
) implements ConstraintJustification {

    public TeacherWeeklyQuotaJustification(String teacher, LocalDate weekStart, Long lessonCount, int maxLessons) {
        this.teacher = teacher;
        this.weekStart = weekStart;
        this.lessonCount = lessonCount;
        this.maxLessons = maxLessons;
    }
    
}
