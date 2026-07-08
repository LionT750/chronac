package org.acme.schooltimetabling.domain;


import ai.timefold.solver.core.api.domain.common.PlanningId;

public class Week {

    @PlanningId
    private Long weekOfYear;

    public Week() {
    }

    public Week(Long weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    public Long getWeekOfYear() {
        return weekOfYear;
    }

    @Override
    public String toString() {
        return "Week " + weekOfYear;
    }
}