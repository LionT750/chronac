package br.com.chronac.solver;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverFactory;
import br.com.chronac.demo.TimetableDemoData;
import br.com.chronac.domain.Block;
import br.com.chronac.domain.Slot;
import br.com.chronac.domain.Timetable;
import br.com.chronac.domain.Track;
import br.com.chronac.service.TimetableGenerator;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The regression anchor for the whole model.
 *
 * This is a timetable built by hand, the way a coordinator would lay the
 * semester out, and it satisfies every rule the business asked for: each turma
 * opens with all of its teachers holding an evening, each UC owns one weekday
 * for its whole run, a UC that ends hands its evening to the next one - and
 * where possible to the same teacher - and the only weekday deviations are the
 * two that the data makes unavoidable, both in the closing weeks.
 *
 * JOVEM PROGRAMADOR - Sala 115, Wed/Thu/Fri, 23 Jul - 13 Dec
 *   Wed  UC4  Alisson x18 --------------------------| idle idle
 *   Thu  UC5  Rodolfo x18 ------------| UC2 Rodolfo x3
 *   Fri  UC1 Vanessa x8 | UC3 Alisson x5 | UC6 Alisson x6 | UC2 Rod x2 (off-home)
 *
 * TECNICO - Sala 114, Mon-Fri, 15 Sep - 8 Mar
 *   Mon  UC10 Alisson x24 ------------------------------| idle
 *   Tue  UC9  Rodolfo x25 ----------------------------------
 *   Wed  UC8  Vanessa x21 --------------------| idle idle    (holes 23 + 30 Sep)
 *   Thu  UC11 Alisson x11 | UC11 Vanessa x11 -----| idle x3
 *   Fri  UC7 Nelma x15 | UC12 Nelma x5 | UC9 Rod x2 (off-home) | idle x3
 *
 * If a change to the constraints makes this layout score worse than it does
 * today, the constraints stopped describing the school.
 */
class TimetableTargetLayoutTest {

    private static final String JOVEM = "Jovem Programador";
    private static final String TECNICO = "Técnico em Desenvolvimento de Sistemas";

    /**
     * What this layout costs, priced by the provider's own weights:
     *   2 idle evenings          - Vanessa is away 23 and 30 September
     *   4 late spilled lessons   - UC2 in December, UC9 in February
     *   5 weeks of spill drift   - UC9 borrows Fridays 5 and 12 February while its
     *                              Tuesday run continues to 2 March (3 + 2 weeks).
     *                              UC2 spills onto the Fridays either side of its
     *                              last Thursday, so it drifts not at all.
     */
    private static final int EXPECTED_SOFT = -(2 * TimetableConstraintProvider.IDLE_EVENING_WEIGHT
            + 4 * TimetableConstraintProvider.OFF_HOME_LATE_WEIGHT
            + 5 * TimetableConstraintProvider.OFF_HOME_DRIFT_WEIGHT);

    @Test
    void handBuiltTargetLayoutIsFeasibleAndScoresAsExpected() {
        Timetable timetable = TimetableDemoData.buildDemoProblem();
        applyTargetLayout(timetable);

        // update() computes the score of a solution the solver did not produce.
        // analyze(), which would give a per-constraint breakdown, is a paid Timefold
        // feature - TimetableConstraintProviderTest covers each constraint instead.
        SolutionManager<Timetable, HardMediumSoftScore> solutionManager =
                SolutionManager.create(SolverFactory.<Timetable>create(TimetableGenerator.baseConfig()));
        HardMediumSoftScore score = solutionManager.update(timetable);

        System.out.println("===== TARGET LAYOUT =====");
        System.out.println("Score           : " + score);
        System.out.println("idle evenings   : " + TimetableGenerator.countIdleEvenings(timetable));
        System.out.println("off-home lessons: " + TimetableGenerator.countOffHomeLessons(timetable));
        System.out.println("holes           : " + TimetableGenerator.countHoles(timetable));
        System.out.println("lessons placed  : " + timetable.getLessons().size());
        System.out.println("=========================");

        assertEquals(0, score.hardScore(),
                "The hand-built target layout must be hard-feasible but scored " + score);
        assertEquals(0, score.mediumScore(),
                "The hand-built target layout must not break any school policy but scored " + score);
        assertEquals(EXPECTED_SOFT, score.softScore(),
                "The hand-built target layout should cost exactly 2 idle evenings, 4 late spilled"
                        + " lessons and 5 weeks of spill drift, but scored " + score);

        // The two idle evenings are Vanessa's blackout on the Tecnico Wednesday
        // track; every other empty evening is a tail and therefore free.
        assertEquals(2, TimetableGenerator.countIdleEvenings(timetable));
        assertEquals(2, TimetableGenerator.countHoles(timetable));
        assertEquals(4, TimetableGenerator.countOffHomeLessons(timetable));
        assertEquals(174, timetable.getLessons().size(), "Every lesson of every UC must be placed");
    }

    /**
     * Sets every block's variables to the layout above. Blocks are addressed by
     * their part id and slots by (turma, weekday, index within the track), so the
     * fixture reads like the diagram.
     */
    static void applyTargetLayout(Timetable timetable) {
        // Jovem Programador
        start(timetable, "UC4#0", JOVEM, DayOfWeek.WEDNESDAY, 0);
        start(timetable, "UC5#0", JOVEM, DayOfWeek.THURSDAY, 0);
        start(timetable, "UC1#0", JOVEM, DayOfWeek.FRIDAY, 0);
        start(timetable, "UC3#0", JOVEM, DayOfWeek.FRIDAY, 8);
        start(timetable, "UC6#0", JOVEM, DayOfWeek.FRIDAY, 13);
        // Rodolfo cannot teach Wednesdays, so his 23 Jovem lessons cannot fit the
        // single 21-slot Thursday track: UC2 closes out on the Friday evenings that
        // UC6 has just vacated. December, i.e. the accepted end-of-semester hotfix.
        start(timetable, "UC2#0", JOVEM, DayOfWeek.THURSDAY, 18);
        overflow(timetable, "UC2#0", JOVEM, DayOfWeek.FRIDAY, 19);

        // Tecnico em Desenvolvimento de Sistemas
        start(timetable, "UC10#0", TECNICO, DayOfWeek.MONDAY, 0);
        // UC9 is 27 lessons and every Tecnico track is 25 slots, so two lessons have
        // to land elsewhere however good the schedule is.
        start(timetable, "UC9#0", TECNICO, DayOfWeek.TUESDAY, 0);
        overflow(timetable, "UC9#0", TECNICO, DayOfWeek.FRIDAY, 20);
        start(timetable, "UC8#0", TECNICO, DayOfWeek.WEDNESDAY, 0);
        start(timetable, "UC11#0", TECNICO, DayOfWeek.THURSDAY, 0);
        start(timetable, "UC11#1", TECNICO, DayOfWeek.THURSDAY, 11);
        start(timetable, "UC7#0", TECNICO, DayOfWeek.FRIDAY, 0);
        start(timetable, "UC12#0", TECNICO, DayOfWeek.FRIDAY, 15);
    }

    private static void start(Timetable timetable, String partId, String turma, DayOfWeek weekday, int index) {
        block(timetable, partId).setStartSlot(slot(timetable, turma, weekday, index));
    }

    /**
     * Points the lessons that did not fit the home weekday at the evening where that
     * short spill run starts. How many spill is derived, not chosen.
     */
    private static void overflow(Timetable timetable, String partId, String turma, DayOfWeek weekday, int index) {
        Block block = block(timetable, partId);
        block.setOverflowStartSlot(slot(timetable, turma, weekday, index));
        assertTrue(block.getOverflowCount() > 0,
                partId + " does not spill anything, so it must not borrow a weekday");
    }

    private static Block block(Timetable timetable, String partId) {
        return timetable.getBlocks().stream()
                .filter(candidate -> candidate.getId().equals(partId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No block " + partId + " in "
                        + timetable.getBlocks()));
    }

    private static Slot slot(Timetable timetable, String turmaName, DayOfWeek weekday, int index) {
        Track track = timetable.getTracks().stream()
                .filter(candidate -> candidate.getTurma().getName().equals(turmaName)
                        && candidate.getDayOfWeek() == weekday)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No track " + turmaName + "-" + weekday));
        Slot slot = track.slotAt(index);
        assertNotNull(slot, "Track " + track + " has no slot " + index + " (size " + track.size() + ")");
        return slot;
    }

}
