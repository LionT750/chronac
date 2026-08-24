package br.com.chronac.domain;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * The single planning entity: one {@link SubjectPart} run of evenings.
 *
 * A block picks the {@link Slot} where it starts and then simply takes the
 * following slots on that same track until its carga horaria is delivered,
 * skipping dates its teacher is unavailable, which leaves a hole. That is what
 * makes "one weekday for the whole run", "no mid-run gap" and "steady weekly
 * cadence" structural properties rather than things the score has to rebuild.
 *
 * When the chosen weekday physically cannot hold the whole UC - Rodolfo needs 23
 * Jovem lessons but cannot teach Wednesdays, and one Thursday track is 21
 * evenings - the leftover lessons spill onto a second weekday as a short
 * contiguous run: the reposicao a coordinator does at the end of a semester.
 *
 * The size of that spill is NOT a decision. It is exactly what did not fit,
 * derived from where the block starts. Letting the solver choose it made
 * deviations a cheap way to free up track slots, and it produced 31 of them
 * where the data forces 4. The only choice is which weekday absorbs the spill.
 */
@PlanningEntity(comparatorClass = Block.DifficultyComparator.class)
public class Block {

    /**
     * The largest spill still worth calling sporadic. Beyond this the placement is
     * rejected outright: a UC leaving its weekday four times is not a hotfix, it is
     * a UC without a home weekday.
     */
    public static final int MAX_OVERFLOW = 3;

    /**
     * Longest run first, then fewest legal evenings to choose from. This is the
     * order a coordinator lays a semester out in - block out the 108h UC before
     * worrying where the 20h one goes - and it lets the construction heuristic land
     * close to feasible before local search starts. Ties break on id so the order
     * is deterministic and a pinned seed reproduces exactly.
     */
    public static class DifficultyComparator implements Comparator<Block> {

        @Override
        public int compare(Block left, Block right) {
            int byLessons = Integer.compare(left.getPart().getLessonCount(),
                    right.getPart().getLessonCount());
            if (byLessons != 0) {
                return byLessons;
            }
            int byFreedom = Integer.compare(right.getAllowedSlots().size(), left.getAllowedSlots().size());
            if (byFreedom != 0) {
                return byFreedom;
            }
            return left.getId().compareTo(right.getId());
        }
    }

    @PlanningId
    private String id;

    private SubjectPart part;

    /**
     * Every evening this block could legally use: the turma tracks on weekdays the
     * UC and this part teacher both allow, inside the UC date window, on dates the
     * teacher is available. Precomputed, so illegal placements are not reachable at
     * all rather than penalized.
     */
    private List<Slot> allowedSlots = List.of();

    /** Dates this part teacher cannot teach; skipped by the run, leaving a hole. */
    private Set<LocalDate> teacherUnavailableDates = Set.of();

    /**
     * Note that a run may start anywhere it is legal, including late enough that most
     * of the UC spills. That lets a UC end up calling its minority weekday home - UC2
     * runs three Thursdays and two Fridays and the model calls Friday its home. It is
     * only a label: the evenings are the same either way, and the score already
     * prefers the other labelling by 13 points, so the solver takes it when it finds
     * it. Restricting this range to slots where at least half the UC fits was tried,
     * and it cost real quality: the best of ten seeds went from -69soft with 2 idle
     * evenings and 5 spills to -91soft with 3 and 6, because the search was using
     * those late starts productively. Left free deliberately.
     */
    @PlanningVariable(valueRangeProviderRefs = "blockSlots")
    private Slot startSlot;

    /**
     * Where the lessons that did not fit the home weekday go. Unassigned in the
     * healthy case, which is why constraints must reach blocks through
     * forEachIncludingUnassigned.
     */
    @PlanningVariable(valueRangeProviderRefs = "blockSlots", allowsUnassigned = true)
    private Slot overflowStartSlot;

    // Derived state is a pure function of the two variables above, but local search
    // asks for it several times per move and each answer walks a whole track. Cache
    // it against the variable values it was computed from - compared on every call,
    // so it stays correct even though Timefold writes the variables reflectively.
    private transient Slot usableCachedFor;
    private transient int usableHomeSlots;
    private transient Slot homeCachedFor;
    private transient List<Slot> homeLessonSlots;
    private transient Slot overflowCachedForStart;
    private transient Slot overflowCachedFor;
    private transient List<Slot> overflowLessonSlots;
    private transient List<PlacedLesson> placedLessons;

    // Required by Timefold
    public Block() {
    }

    public Block(SubjectPart part, List<Slot> allowedSlots, Set<LocalDate> teacherUnavailableDates) {
        this.id = part.getId();
        this.part = part;
        this.allowedSlots = List.copyOf(allowedSlots);
        this.teacherUnavailableDates = Set.copyOf(teacherUnavailableDates);
    }

    /** Every evening this block could use, for a run or for a borrowed spill. */
    @ValueRangeProvider(id = "blockSlots")
    @JsonIgnore
    public List<Slot> getAllowedSlots() {
        return allowedSlots;
    }

    // ************************************************************************
    // Derived state - pure functions of the two planning variables
    // ************************************************************************

    @JsonIgnore
    public Track getTrack() {
        return startSlot == null ? null : startSlot.getTrack();
    }

    public int getStartIndex() {
        return startSlot == null ? -1 : startSlot.getIndex();
    }

    /**
     * Evenings the home weekday can still offer from the start slot onwards, ignoring
     * dates the teacher cannot teach. This is what decides how much has to spill.
     */
    public int getUsableHomeSlots() {
        if (startSlot == null) {
            return 0;
        }
        if (usableCachedFor != startSlot) {
            usableHomeSlots = countUsable(startSlot, Integer.MAX_VALUE);
            usableCachedFor = startSlot;
        }
        return usableHomeSlots;
    }

    /** Lessons delivered on the home weekday: everything that fits there. */
    public int getHomeLessonCount() {
        return Math.min(part.getLessonCount(), getUsableHomeSlots());
    }

    /** Lessons that did not fit the home weekday and must spill onto another one. */
    public int getOverflowCount() {
        return part.getLessonCount() - getHomeLessonCount();
    }

    /**
     * The evenings on the home track that carry a lesson: consecutive slots from the
     * start slot, skipping the teacher unavailable dates.
     */
    @JsonIgnore
    public List<Slot> getHomeLessonSlots() {
        if (homeLessonSlots == null || homeCachedFor != startSlot) {
            homeLessonSlots = collect(startSlot, getHomeLessonCount());
            homeCachedFor = startSlot;
            placedLessons = null;
        }
        return homeLessonSlots;
    }

    /** The short contiguous run on the borrowed weekday, empty when nothing spilled. */
    @JsonIgnore
    public List<Slot> getOverflowLessonSlots() {
        if (overflowLessonSlots == null
                || overflowCachedForStart != startSlot
                || overflowCachedFor != overflowStartSlot) {
            overflowLessonSlots = collect(overflowStartSlot, getOverflowCount());
            overflowCachedForStart = startSlot;
            overflowCachedFor = overflowStartSlot;
            placedLessons = null;
        }
        return overflowLessonSlots;
    }

    /**
     * Spilled lessons that still have nowhere to go: either no borrowed weekday was
     * chosen, or the one chosen runs off the end of its track. Non-zero is a hard
     * violation, and its size is the gradient the solver descends.
     */
    public int getOverflowShortfall() {
        return getOverflowCount() - getOverflowLessonSlots().size();
    }

    /** Spilled lessons beyond what still counts as sporadic. */
    public int getOverflowExcess() {
        return Math.max(0, getOverflowCount() - MAX_OVERFLOW);
    }

    /** Index of the last home lesson, or the start index when the block has none. */
    public int getEndIndex() {
        List<Slot> home = getHomeLessonSlots();
        return home.isEmpty() ? getStartIndex() : home.get(home.size() - 1).getIndex();
    }

    /** Skipped evenings strictly inside the run, caused by teacher unavailability. */
    public int getHoles() {
        List<Slot> home = getHomeLessonSlots();
        if (home.size() < 2) {
            return 0;
        }
        return (getEndIndex() - getStartIndex() + 1) - home.size();
    }

    /** Every evening this block delivers, home run plus spill. */
    @JsonIgnore
    public List<PlacedLesson> getPlacedLessons() {
        List<Slot> home = getHomeLessonSlots();
        List<Slot> spill = getOverflowLessonSlots();
        if (placedLessons == null) {
            List<PlacedLesson> out = new ArrayList<>(home.size() + spill.size());
            for (Slot slot : home) {
                out.add(new PlacedLesson(this, slot, false));
            }
            for (Slot slot : spill) {
                out.add(new PlacedLesson(this, slot, true));
            }
            placedLessons = out;
        }
        return placedLessons;
    }

    /** The date this block starts teaching, used to order handoffs across tracks. */
    @JsonIgnore
    public LocalDate getFirstHomeLessonDate() {
        List<Slot> home = getHomeLessonSlots();
        return home.isEmpty() ? null : home.get(0).getDate();
    }

    /**
     * The date of the last home lesson, used to judge whether a spill sits at the end
     * of the run (a hotfix) or in the middle of the semester (drift).
     */
    @JsonIgnore
    public LocalDate getLastHomeLessonDate() {
        List<Slot> home = getHomeLessonSlots();
        return home.isEmpty() ? null : home.get(home.size() - 1).getDate();
    }

    public boolean placesLessonInWeek(long weekIndex) {
        for (PlacedLesson placed : getPlacedLessons()) {
            if (placed.slot().getWeekIndex() == weekIndex) {
                return true;
            }
        }
        return false;
    }

    /** Walks a track from {@code from}, collecting up to {@code wanted} teachable evenings. */
    private List<Slot> collect(Slot from, int wanted) {
        if (from == null || wanted <= 0) {
            return List.of();
        }
        List<Slot> out = new ArrayList<>(wanted);
        Track track = from.getTrack();
        for (int i = from.getIndex(); i < track.size() && out.size() < wanted; i++) {
            Slot slot = track.slotAt(i);
            if (!teacherUnavailableDates.contains(slot.getDate())) {
                out.add(slot);
            }
        }
        return out;
    }

    private int countUsable(Slot from, int limit) {
        int count = 0;
        Track track = from.getTrack();
        for (int i = from.getIndex(); i < track.size() && count < limit; i++) {
            if (!teacherUnavailableDates.contains(track.slotAt(i).getDate())) {
                count++;
            }
        }
        return count;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public SubjectPart getPart() {
        return part;
    }

    @JsonIgnore
    public Set<LocalDate> getTeacherUnavailableDates() {
        return teacherUnavailableDates;
    }

    public Slot getStartSlot() {
        return startSlot;
    }

    public void setStartSlot(Slot startSlot) {
        this.startSlot = startSlot;
    }

    public Slot getOverflowStartSlot() {
        return overflowStartSlot;
    }

    public void setOverflowStartSlot(Slot overflowStartSlot) {
        this.overflowStartSlot = overflowStartSlot;
    }

    @Override
    public String toString() {
        return id;
    }
}
