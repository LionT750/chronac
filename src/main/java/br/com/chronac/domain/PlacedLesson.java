package br.com.chronac.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * A single evening that a {@link Block} actually delivers: which block, which
 * {@link Slot}, and whether it sits on the block home track or is a one-off
 * deviation onto another weekday (a reposicao).
 *
 * Blocks are the planning entities, but almost every constraint reasons about
 * individual evenings, so the constraint provider flattens each block into
 * these. Nothing here is planned - it is all derived from the block variables.
 */
public record PlacedLesson(Block block, Slot slot, boolean offHome) {

    public SubjectPart part() {
        return block.getPart();
    }

    public Subject subject() {
        return block.getPart().getSubject();
    }

    public Turma turma() {
        return block.getPart().getSubject().getTurma();
    }

    public String teacher() {
        return block.getPart().getTeacher();
    }

    public LocalDate date() {
        return slot.getDate();
    }

    public DayOfWeek dayOfWeek() {
        return slot.getDayOfWeek();
    }

    public long weekIndex() {
        return slot.getWeekIndex();
    }

    public Track track() {
        return slot.getTrack();
    }
}
