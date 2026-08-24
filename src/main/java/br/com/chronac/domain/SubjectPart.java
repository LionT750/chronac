package br.com.chronac.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * One teacher's share of a UC. Most UCs have a single part; a UC that is split
 * between two teachers has one part each, taught in order - UC11 is Alisson's
 * 42h followed by Vanessa's 42h, one continuous UC from the students' side that
 * changes teacher halfway.
 *
 * A part is what actually gets scheduled: there is exactly one {@link Block}
 * per part.
 */
public class SubjectPart {

    public static final int HOURS_PER_LESSON = 4;

    @PlanningId
    private final String id;

    private final Subject subject;
    private final int index;
    private final String teacher;
    private final int totalHours;
    private final int lessonCount;

    public SubjectPart(Subject subject, int index, String teacher, int totalHours) {
        this.subject = subject;
        this.index = index;
        this.teacher = teacher;
        this.totalHours = totalHours;
        this.lessonCount = Math.ceilDiv(totalHours, HOURS_PER_LESSON);
        this.id = subject.getName() + "#" + index;
    }

    public String getId() {
        return id;
    }

    @JsonIgnore
    public Subject getSubject() {
        return subject;
    }

    /** 0-based position of this part within its UC; parts are taught in this order. */
    public int getIndex() {
        return index;
    }

    public String getTeacher() {
        return teacher;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    @Override
    public String toString() {
        return id + "(" + teacher + ")";
    }
}
