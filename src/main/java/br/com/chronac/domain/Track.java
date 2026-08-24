package br.com.chronac.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.DayOfWeek;
import java.util.List;

/**
 * A track is one turma's weekday lane: "Jovem Programador on Wednesdays". It
 * owns the ordered list of evenings ({@link Slot}s) available on that weekday
 * inside the turma's calendar window.
 *
 * The whole model rests on this: a turma runs one track per weekday it uses, a
 * subject occupies a contiguous run of one track, and when a subject exhausts
 * its carga horaria the next subject takes over the very next slot. Tracks are
 * problem facts - the solver never invents or removes one.
 */
public class Track {

    @PlanningId
    private final String id;

    private final Turma turma;
    private final DayOfWeek dayOfWeek;
    private final List<Slot> slots;

    public Track(Turma turma, DayOfWeek dayOfWeek, List<java.time.LocalDate> dates) {
        this.turma = turma;
        this.dayOfWeek = dayOfWeek;
        this.id = turma.getName() + "-" + dayOfWeek;
        List<Slot> built = new java.util.ArrayList<>(dates.size());
        for (int i = 0; i < dates.size(); i++) {
            built.add(new Slot(this, i, dates.get(i)));
        }
        this.slots = List.copyOf(built);
    }

    public String getId() {
        return id;
    }

    @JsonIgnore
    public Turma getTurma() {
        return turma;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    @JsonIgnore
    public List<Slot> getSlots() {
        return slots;
    }

    public int size() {
        return slots.size();
    }

    /** The slot at {@code index}, or null when the index runs past the end of the track. */
    public Slot slotAt(int index) {
        return index >= 0 && index < slots.size() ? slots.get(index) : null;
    }

    @Override
    public String toString() {
        return id;
    }
}
