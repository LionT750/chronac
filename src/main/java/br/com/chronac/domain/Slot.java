package br.com.chronac.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * One teachable evening on one {@link Track}: "Sala 115, Wednesday, the 3rd
 * Wednesday of the run". Slots are the planning values a {@link Block} chooses
 * from, and they are the unit of room occupancy - a slot carries at most one
 * lesson, because a track belongs to exactly one turma and therefore one room.
 *
 * A slot's {@code index} is its position within its track, which is what makes
 * a block a cheap contiguous range instead of a set of dates.
 */
public class Slot {

    @PlanningId
    private final String id;

    private final Track track;
    private final int index;
    private final LocalDate date;

    public Slot(Track track, int index, LocalDate date) {
        this.track = track;
        this.index = index;
        this.date = date;
        this.id = track.getId() + "#" + index;
    }

    public String getId() {
        return id;
    }

    /** Back-reference to the owning track; ignored by Jackson to avoid a cycle. */
    @JsonIgnore
    public Track getTrack() {
        return track;
    }

    public int getIndex() {
        return index;
    }

    public LocalDate getDate() {
        return date;
    }

    public DayOfWeek getDayOfWeek() {
        return track.getDayOfWeek();
    }

    /**
     * A monotonically increasing week index (epoch weeks, Monday-based) rather than
     * an ISO week-of-year, so consecutive calendar weeks always differ by exactly 1
     * even across the New Year.
     */
    public long getWeekIndex() {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay() / 7;
    }

    @Override
    public String toString() {
        return id + "(" + date + ")";
    }
}
