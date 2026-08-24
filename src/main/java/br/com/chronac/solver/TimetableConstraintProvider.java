package br.com.chronac.solver;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream;

import org.jspecify.annotations.NonNull;

import java.time.LocalDate;

import br.com.chronac.domain.Block;
import br.com.chronac.domain.PlacedLesson;
import br.com.chronac.domain.Slot;
import br.com.chronac.domain.SubjectPart;
import br.com.chronac.domain.Turma;

/**
 * The rules a real school timetable follows.
 *
 * Most of what used to live here is gone, because the model now makes it true by
 * construction: a {@link Block} occupies a contiguous run of one weekday track,
 * so "one weekday per UC", "no mid-run gap" and "steady weekly cadence" are not
 * things to score any more. What is left are the genuine trade-offs, and every
 * soft weight counts EVENTS you can point at on the calendar - one idle evening,
 * one deviation, one avoidable handoff - never lesson totals. That is why a good
 * solution scores in the tens rather than the tens of thousands.
 *
 * Weights can be retuned at runtime through the solution's
 * ConstraintWeightOverrides without recompiling; the values here are the defaults.
 */
public class TimetableConstraintProvider implements ConstraintProvider {

    // ----- Constraint names, also the keys for weight overrides -----
    public static final String OVERFLOW_STAYS_SPORADIC = "Spill off the home weekday stays sporadic";
    public static final String BLOCKS_DO_NOT_OVERLAP = "Two blocks overlap on one track";
    public static final String ONE_LESSON_PER_EVENING = "One lesson per evening in a room";
    public static final String TEACHER_SINGLE_BOOKED = "Teacher cannot be in two places at once";
    public static final String UC_PARTS_CHAINED = "UC parts run back to back on one track";
    public static final String TEACHERS_LIVE_FIRST_WEEK = "Every turma teacher is live in the first full week";
    public static final String OVERFLOW_WELL_PLACED = "Spilled lessons must land on another weekday that can hold them";
    public static final String OFF_HOME_MID_SEMESTER = "Off-home lesson mid-semester";
    public static final String IDLE_EVENING = "Idle evening inside a live track";
    public static final String AVOIDABLE_HANDOFF = "Handoff while the outgoing teacher still had subjects left";
    public static final String OFF_HOME_LATE = "Off-home lesson as a late-semester hotfix";
    public static final String OFF_HOME_DRIFT = "Spilled lesson sits far from the end of its run";

    /**
     * An evening left empty in the middle of a track that is still running. The
     * dearest soft break: when one UC finishes its carga horaria the next should
     * take over that evening, so a hole means the school lost a night.
     */
    static final int IDLE_EVENING_WEIGHT = 8;

    /**
     * A track changing teacher while the outgoing teacher still had an unstarted
     * subject in that turma - the new teacher jumped the queue.
     */
    static final int AVOIDABLE_HANDOFF_WEIGHT = 6;

    /**
     * A spilled lesson in the closing weeks of a run: the accepted hotfix.
     *
     * Priced above an idle evening. Owning a weekday is what students and teachers
     * plan around, so a schedule that keeps every UC on its own evening and leaves a
     * night empty beats one that fills the night by moving a UC.
     */
    static final int OFF_HOME_LATE_WEIGHT = 10;

    /**
     * A deviation anywhere earlier. Medium, not soft: shifting a UC off its weekday
     * mid-semester is never worth trading for a preference, but it is not
     * physically impossible either.
     */
    static final int OFF_HOME_MID_SEMESTER_WEIGHT = 1;

    /**
     * Per week between a spilled lesson and the end of its own run. Small, but it is
     * the gradient that walks a spill towards the end of the semester: the flat
     * mid-semester charge alone tells the solver a spill is wrong without telling it
     * which direction is better, and it settled for spills months out of place.
     */
    static final int OFF_HOME_DRIFT_WEIGHT = 1;

    /**
     * How close to the end of its own run a deviation has to be to count as a
     * late-semester hotfix rather than mid-semester drift.
     */
    static final int LATE_WINDOW_DAYS = 28;

    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory factory) {
        return new Constraint[] {
                // HARD - physically impossible
                overflowStaysSporadic(factory),
                blocksDoNotOverlap(factory),
                oneLessonPerEvening(factory),
                teacherSingleBooked(factory),
                ucPartsChained(factory),
                teachersLiveInFirstFullWeek(factory),
                overflowWellPlaced(factory),

                // MEDIUM - school policy, never traded for a preference
                offHomeMidSemester(factory),

                // SOFT - preferences, event counts
                idleEveningInsideLiveTrack(factory),
                avoidableHandoff(factory),
                offHomeLate(factory),
                offHomeDriftsFromRunEnd(factory),
        };
    }

    /**
     * Every block that has been given a starting evening.
     *
     * Deliberately not the plain forEach: that silently filters out any entity with
     * a null genuine variable, and a block's borrowed weekday is normally unassigned
     * - a UC that fits its own evening is the good case. Under forEach every
     * well-behaved block was invisible to every constraint.
     */
    private static UniConstraintStream<Block> placedBlocks(ConstraintFactory factory) {
        return factory.forEachIncludingUnassigned(Block.class)
                .filter(block -> block.getStartSlot() != null);
    }

    /**
     * Every evening the plan actually delivers. Blocks are the planning entities,
     * but the rules are about individual nights, so flatten each block into its home
     * run plus whatever spilled onto a borrowed weekday.
     */
    private static UniConstraintStream<PlacedLesson> lessons(ConstraintFactory factory) {
        return placedBlocks(factory)
                .flattenLast(Block::getPlacedLessons);
    }

    // -------------------------
    // HARD CONSTRAINTS
    // -------------------------

    Constraint overflowStaysSporadic(ConstraintFactory factory) {
        // The UC spilled more lessons off its weekday than counts as sporadic. A UC
        // leaving its home evening four or more times has no home evening; reject the
        // placement, and penalize by the excess so the solver has a gradient pushing
        // the block to an earlier start where more of it fits.
        return placedBlocks(factory)
                .filter(block -> block.getOverflowExcess() > 0)
                .penalize(HardMediumSoftScore.ONE_HARD, Block::getOverflowExcess)
                .asConstraint(OVERFLOW_STAYS_SPORADIC);
    }

    Constraint blocksDoNotOverlap(ConstraintFactory factory) {
        // Two UCs cannot share a stretch of the same weekday: a track carries one
        // subject at a time and hands over cleanly. This is also the room conflict,
        // because a track belongs to exactly one turma and therefore one room.
        return placedBlocks(factory)
                .join(placedBlocks(factory),
                        Joiners.equal(Block::getTrack),
                        // Stands in for forEachUniquePair, which cannot be used here:
                        // it is built on the plain forEach and would drop every block.
                        Joiners.lessThan(Block::getId))
                .filter((first, second) -> first.getStartIndex() <= second.getEndIndex()
                        && second.getStartIndex() <= first.getEndIndex())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (first, second) -> Math.min(first.getEndIndex(), second.getEndIndex())
                                - Math.max(first.getStartIndex(), second.getStartIndex()) + 1L)
                .asConstraint(BLOCKS_DO_NOT_OVERLAP);
    }

    Constraint oneLessonPerEvening(ConstraintFactory factory) {
        // A slot is one room on one evening, so it carries at most one lesson. Block
        // overlap covers the runs; this also catches a deviation landing on an
        // evening that is already taken.
        return lessons(factory)
                .groupBy(PlacedLesson::slot, ConstraintCollectors.count())
                .filter((slot, count) -> count > 1)
                .penalize(HardMediumSoftScore.ONE_HARD, (slot, count) -> count - 1)
                .asConstraint(ONE_LESSON_PER_EVENING);
    }

    Constraint teacherSingleBooked(ConstraintFactory factory) {
        // The only rule that couples the two turmas: both run on the same evenings
        // and share three of their four teachers, so a teacher cannot hold Sala 114
        // and Sala 115 on the same night.
        return lessons(factory)
                .groupBy(PlacedLesson::teacher, PlacedLesson::date, ConstraintCollectors.count())
                .filter((teacher, date, count) -> count > 1)
                .penalize(HardMediumSoftScore.ONE_HARD, (teacher, date, count) -> count - 1)
                .asConstraint(TEACHER_SINGLE_BOOKED);
    }

    Constraint ucPartsChained(ConstraintFactory factory) {
        // A UC split between two teachers is still ONE UC: both parts sit on the same
        // weekday track and the second starts the very next evening after the first
        // ends. UC11 is Alisson's 42h followed straight away by Vanessa's 42h.
        return placedBlocks(factory)
                .join(placedBlocks(factory),
                        Joiners.equal(block -> block.getPart().getSubject(),
                                block -> block.getPart().getSubject()),
                        Joiners.equal(block -> block.getPart().getIndex() + 1,
                                block -> block.getPart().getIndex()))
                .filter((previous, next) -> previous.getTrack() != next.getTrack()
                        || next.getStartIndex() != previous.getEndIndex() + 1)
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint(UC_PARTS_CHAINED);
    }

    Constraint teachersLiveInFirstFullWeek(ConstraintFactory factory) {
        // A turma opens with every one of its teachers already holding an evening, and
        // subjects hand off from there. Measured from the turma's first FULL week,
        // because Jovem Programador opens on a Thursday with only two evenings
        // available to three teachers.
        //
        // Penalized by how many weeks late the teacher actually starts, not as a flat
        // yes/no. A teacher who appears one week late is nearly right and a teacher who
        // appears in November is not, and local search needs to be able to tell the
        // difference to walk towards the fix.
        return lessons(factory)
                .groupBy(PlacedLesson::turma, PlacedLesson::teacher,
                        ConstraintCollectors.min(PlacedLesson::weekIndex))
                .filter((turma, teacher, firstWeek) -> firstWeek > turma.getFirstViableWeek())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (turma, teacher, firstWeek) -> firstWeek - turma.getFirstViableWeek())
                .asConstraint(TEACHERS_LIVE_FIRST_WEEK);
    }

    Constraint overflowWellPlaced(ConstraintFactory factory) {
        // Whatever did not fit the home weekday has to actually land somewhere: on a
        // DIFFERENT weekday (spilling further along the same one would just be a gap
        // in the run), with enough teachable evenings left there to hold it. And a
        // borrowed weekday with nothing to put on it is meaningless.
        return placedBlocks(factory)
                .filter(block -> overflowDefects(block) > 0)
                .penalize(HardMediumSoftScore.ONE_HARD, TimetableConstraintProvider::overflowDefects)
                .asConstraint(OVERFLOW_WELL_PLACED);
    }

    // -------------------------
    // MEDIUM CONSTRAINTS
    // -------------------------

    Constraint offHomeMidSemester(ConstraintFactory factory) {
        // Changing which evening a UC uses in the middle of the semester is the one
        // realism break nobody accepts: students and teachers plan around owning
        // "Thursday evening". Medium, so no soft preference can ever buy it.
        return lessons(factory)
                .filter(placed -> placed.offHome() && !isLateInRun(placed))
                .penalize(HardMediumSoftScore.ofMedium(OFF_HOME_MID_SEMESTER_WEIGHT))
                .asConstraint(OFF_HOME_MID_SEMESTER);
    }

    // -------------------------
    // SOFT CONSTRAINTS
    // -------------------------

    Constraint idleEveningInsideLiveTrack(ConstraintFactory factory) {
        // Count the evenings a live track leaves empty: from its first lesson to its
        // last, how many of those slots carry nothing. Deliberately measured between
        // the first and last lesson, so ramp-up before a track opens and the taper
        // after its last subject finishes are both free - only a hole in a running
        // track costs anything.
        return lessons(factory)
                .groupBy(PlacedLesson::track,
                        ConstraintCollectors.min((PlacedLesson placed) -> placed.slot().getIndex()),
                        ConstraintCollectors.max((PlacedLesson placed) -> placed.slot().getIndex()),
                        ConstraintCollectors.count())
                .filter((track, first, last, count) -> last - first + 1 > count)
                .penalize(HardMediumSoftScore.ofSoft(IDLE_EVENING_WEIGHT),
                        (track, first, last, count) -> last - first + 1L - count)
                .asConstraint(IDLE_EVENING);
    }

    Constraint avoidableHandoff(ConstraintFactory factory) {
        // When a UC ends and the next one takes over that evening, it should be the
        // same teacher continuing if they still have a subject to teach. Bringing in
        // a different teacher while the outgoing one still has an unstarted subject
        // in that turma is the handoff a coordinator would not make.
        return placedBlocks(factory)
                .join(placedBlocks(factory),
                        Joiners.equal(Block::getTrack),
                        Joiners.equal(block -> block.getEndIndex() + 1, Block::getStartIndex))
                .filter((outgoing, incoming) ->
                        !outgoing.getPart().getTeacher().equals(incoming.getPart().getTeacher())
                                // Two parts of one UC changing teacher is the point of the UC,
                                // not a handoff; ucPartsChained already governs it.
                                && outgoing.getPart().getSubject() != incoming.getPart().getSubject())
                .ifExists(placedBlocks(factory),
                        Joiners.equal((Block outgoing, Block incoming) -> outgoing.getPart().getTeacher(),
                                block -> block.getPart().getTeacher()),
                        Joiners.filtering((outgoing, incoming, pending) ->
                                pending != outgoing
                                        && pending.getPart().getSubject().getTurma()
                                                == outgoing.getPart().getSubject().getTurma()
                                        && startsAfter(pending, outgoing)))
                .penalize(HardMediumSoftScore.ofSoft(AVOIDABLE_HANDOFF_WEIGHT))
                .asConstraint(AVOIDABLE_HANDOFF);
    }

    Constraint offHomeLate(ConstraintFactory factory) {
        // The accepted case: a UC borrows a neighbouring evening for its last lesson
        // or two, in the closing weeks. Cheap, but not free - a schedule that needs
        // none of these is still better.
        return lessons(factory)
                .filter(placed -> placed.offHome() && isLateInRun(placed))
                .penalize(HardMediumSoftScore.ofSoft(OFF_HOME_LATE_WEIGHT))
                .asConstraint(OFF_HOME_LATE);
    }

    Constraint offHomeDriftsFromRunEnd(ConstraintFactory factory) {
        // A borrowed evening belongs next to the end of the run it belongs to. The
        // flat mid-semester charge says "not here" but not "closer to December", so
        // charge a week at a time for the distance and local search can walk it home.
        return lessons(factory)
                .filter(PlacedLesson::offHome)
                .penalize(HardMediumSoftScore.ofSoft(OFF_HOME_DRIFT_WEIGHT),
                        TimetableConstraintProvider::weeksFromRunEnd)
                .asConstraint(OFF_HOME_DRIFT);
    }

    // -------------------------
    // HELPERS
    // -------------------------

    /** Whole weeks between a spilled lesson and the last evening of its home run. */
    static long weeksFromRunEnd(PlacedLesson placed) {
        LocalDate lastHome = placed.block().getLastHomeLessonDate();
        if (lastHome == null) {
            return 0;
        }
        return Math.abs(java.time.temporal.ChronoUnit.DAYS.between(lastHome, placed.date())) / 7;
    }

    /**
     * Counts the ways a block's spill is misplaced. Purely structural, so one hard
     * constraint can price them all together and the count gives local search a
     * gradient instead of a cliff.
     */
    static long overflowDefects(Block block) {
        Slot overflowStart = block.getOverflowStartSlot();
        if (block.getOverflowCount() == 0) {
            // Nothing spilled, so nothing may be booked on a borrowed weekday.
            return overflowStart == null ? 0 : 1;
        }
        if (overflowStart == null) {
            return block.getOverflowCount();
        }
        long defects = block.getOverflowShortfall();
        if (overflowStart.getTrack() == block.getTrack()) {
            defects++;
        }
        return defects;
    }

    /**
     * Whether a deviation sits in the closing weeks of its own block's run. Measured
     * against the block's last home lesson rather than the semester end, so a UC
     * that finishes in October is judged on its own calendar.
     *
     * Bounded on BOTH sides: a lower bound alone let a deviation months after the
     * run ended count as "late", which is drift, not a hotfix.
     */
    static boolean isLateInRun(PlacedLesson placed) {
        LocalDate lastHome = placed.block().getLastHomeLessonDate();
        if (lastHome == null) {
            return false;
        }
        return !placed.date().isBefore(lastHome.minusDays(LATE_WINDOW_DAYS - 1L))
                && !placed.date().isAfter(lastHome.plusDays(LATE_WINDOW_DAYS - 1L));
    }

    /** Whether {@code pending} only starts teaching after {@code outgoing} has finished. */
    static boolean startsAfter(Block pending, Block outgoing) {
        LocalDate pendingStart = pending.getFirstHomeLessonDate();
        LocalDate outgoingEnd = outgoing.getLastHomeLessonDate();
        return pendingStart != null && outgoingEnd != null && pendingStart.isAfter(outgoingEnd);
    }
}
