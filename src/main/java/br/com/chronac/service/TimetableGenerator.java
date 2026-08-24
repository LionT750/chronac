package br.com.chronac.service;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.constructionheuristic.placer.QueuedEntityPlacerConfig;
import ai.timefold.solver.core.config.heuristic.selector.common.SelectionCacheType;
import ai.timefold.solver.core.config.heuristic.selector.common.SelectionOrder;
import ai.timefold.solver.core.config.heuristic.selector.entity.EntitySelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.entity.EntitySorterManner;
import ai.timefold.solver.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.value.ValueSelectorConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import br.com.chronac.domain.Block;
import br.com.chronac.domain.Timetable;
import br.com.chronac.domain.Track;
import br.com.chronac.solver.TimetableConstraintProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Seedable, synchronous timetable generator. Each solve uses an explicit random
 * seed so a run is reproducible - which is what lets a good seed be pinned for
 * the demo instead of re-solving from scratch every time.
 */
@Service
public class TimetableGenerator {

    /**
     * The seed pinned for the demo. Found by GET /api/find-perfect and kept here so
     * the demo shows the same known-good timetable every time.
     *
     * Seed 5 lands on 0hard/0medium/-69soft: two idle evenings, five lessons spilled
     * off their weekday and two evenings skipped for Vanessa being away - which is
     * within one spilled lesson of the best layout anyone has built by hand for this
     * data (see TimetableTargetLayoutTest). Re-run /api/find-perfect and update this
     * whenever the demo data changes.
     */
    public static final long DEMO_SEED = 5L;

    /**
     * How long the pinned demo solve gets, and how long without improvement before it
     * gives up early.
     *
     * A step count was tried here, because it would make the pinned seed reproduce
     * the identical timetable on any machine. It cannot be calibrated on this
     * problem: the cost of a step explodes as the schedule approaches feasible
     * (~6500 steps a second while still at -3 hard, ~900 a second once near zero), so
     * no step target means "converged". What the seed does fix is the search
     * trajectory - given enough time the same seed plateaus in the same place.
     *
     * Seed 5 reaches 0hard/0medium around the 45 second mark and does not improve
     * after that, so 25 seconds without progress is a safe early exit and 90 seconds
     * is a cap for a slower machine.
     */
    public static final long DEMO_BUDGET_SECONDS = 90L;
    public static final long DEMO_UNIMPROVED_SECONDS = 25L;

    private static final String START_SLOT = "startSlot";
    private static final String OVERFLOW_START_SLOT = "overflowStartSlot";

    public record GenerationResult(Timetable timetable, long seed, boolean feasible, long hardScore,
            long mediumScore, long softScore, long idleEvenings, long offHomeLessons, long holes) {
    }

    /** The solver setup, also used by tests that need to score a hand-built solution. */
    public static SolverConfig baseConfig() {
        return new SolverConfig()
                .withSolutionClass(Timetable.class)
                .withEntityClassList(List.of(Block.class))
                .withConstraintProviderClass(TimetableConstraintProvider.class)
                // NO_ASSERT: the default PHASE_ASSERT re-verifies the score after every
                // phase, which costs far more than it is worth here. Still fully
                // reproducible for a given random seed.
                .withEnvironmentMode(EnvironmentMode.NO_ASSERT)
                .withPhaseList(List.of(
                        constructionHeuristicFor(START_SLOT),
                        constructionHeuristicFor(OVERFLOW_START_SLOT),
                        localSearch()));
    }

    /**
     * Moving a UC to a different weekday and moving its borrowed evening are separate
     * decisions, so each variable gets its own change selector; swapping start slots
     * between two UCs is what exchanges two weekday lanes in one step.
     *
     * Ruin and recreate was tried here and made things markedly worse - it can only
     * ruin one variable, so it left borrowed evenings pointing at nothing and the
     * hard score went from -1 to -8.
     */
    private static LocalSearchPhaseConfig localSearch() {
        // Block has two variables, so every selector has to name the one it works on.
        return new LocalSearchPhaseConfig()
                .withMoveSelectorConfig(new UnionMoveSelectorConfig(List.of(
                        new ChangeMoveSelectorConfig()
                                .withValueSelectorConfig(new ValueSelectorConfig(START_SLOT)),
                        new ChangeMoveSelectorConfig()
                                .withValueSelectorConfig(new ValueSelectorConfig(OVERFLOW_START_SLOT)),
                        swapStartSlots())));
    }

    private static SwapMoveSelectorConfig swapStartSlots() {
        SwapMoveSelectorConfig swap = new SwapMoveSelectorConfig();
        swap.setVariableNameIncludeList(List.of(START_SLOT));
        return swap;
    }

    /**
     * Places the longest runs first - exactly how a coordinator lays out a semester -
     * assigning ONE variable.
     *
     * Two things make this the explicit config rather than plain FIRST_FIT_DECREASING.
     * It would take the cartesian product of both of a block's variables, ~15000
     * placements each; and it is run twice, once to choose every UC's home weekday
     * and again to place whatever spilled. Doing the spill in its own pass matters:
     * left to local search, the phase handed over a solution where every spilled
     * lesson was still homeless, and it spent its time climbing out of that instead
     * of improving the schedule.
     */
    private static ConstructionHeuristicPhaseConfig constructionHeuristicFor(String variableName) {
        return new ConstructionHeuristicPhaseConfig()
                .withEntityPlacerConfig(new QueuedEntityPlacerConfig()
                        .withEntitySelectorConfig(new EntitySelectorConfig(Block.class)
                                .withCacheType(SelectionCacheType.PHASE)
                                .withSelectionOrder(SelectionOrder.SORTED)
                                .withSorterManner(EntitySorterManner.DESCENDING))
                        .withMoveSelectorConfigList(List.of(
                                new ChangeMoveSelectorConfig()
                                        .withValueSelectorConfig(new ValueSelectorConfig(variableName)))));
    }

    /**
     * Solves with an explicit random seed, stopping once the search has gone
     * unimprovedSeconds without finding anything better, or when the budget runs out.
     */
    public Timetable solveSeeded(Timetable problem, long seed, long budgetSeconds, long unimprovedSeconds) {
        SolverConfig config = baseConfig()
                .withRandomSeed(seed)
                .withTerminationConfig(new TerminationConfig()
                        .withSecondsSpentLimit(budgetSeconds)
                        .withUnimprovedSecondsSpentLimit(unimprovedSeconds));
        return onThreadThatCanSeeOurClasses(() -> {
            Solver<Timetable> solver = SolverFactory.<Timetable>create(config).buildSolver();
            return solver.solve(problem);
        });
    }

    /**
     * Runs the solve with a context classloader that can actually load our domain
     * classes.
     *
     * SolverConfig does not keep the Class objects it is given: setSolutionClass
     * stores getName() and getSolutionClass() resolves the string again with
     * Class.forName(name, false, Thread.currentThread().getContextClassLoader()).
     * On a thread whose context classloader is the system one - a ForkJoinPool
     * common-pool worker, say - that lookup fails inside a Spring Boot fat jar,
     * because the application classes live in BOOT-INF/classes and only Spring's
     * launcher classloader can see them. The jar started fine and then died with
     * "The solutionClass (br.com.chronac.domain.Timetable) cannot be found", while
     * every test passed because a test classpath is flat.
     *
     * Guarding it here rather than at the call site keeps the generator safe to call
     * from any thread, which is the property that was actually missing.
     */
    private static <T> T onThreadThatCanSeeOurClasses(Supplier<T> solve) {
        Thread current = Thread.currentThread();
        ClassLoader original = current.getContextClassLoader();
        current.setContextClassLoader(Timetable.class.getClassLoader());
        try {
            return solve.get();
        } finally {
            current.setContextClassLoader(original);
        }
    }

    /** Solves with the budget pinned for the demo. */
    public Timetable solveSeeded(Timetable problem, long seed) {
        return solveSeeded(problem, seed, DEMO_BUDGET_SECONDS, DEMO_UNIMPROVED_SECONDS);
    }

    /**
     * Scans seeds keeping the best feasible timetable found, ranked on the score
     * itself - that score already prices idle evenings, handoff quality and
     * off-home deviations against each other, so ranking on any single extracted
     * count would let a solution win on one metric while being worse on every
     * other. Falls back to the overall best if none is feasible. Seeds start at 1.
     */
    public GenerationResult findPerfect(Supplier<Timetable> problemSupplier, int maxSeeds, long secondsPerSeed) {
        GenerationResult best = null;
        GenerationResult bestFeasible = null;
        for (int i = 0; i < maxSeeds; i++) {
            long seed = i + 1L;
            GenerationResult result = describe(solveSeeded(problemSupplier.get(), seed, secondsPerSeed,
                    DEMO_UNIMPROVED_SECONDS), seed);
            if (best == null || isBetter(result, best)) {
                best = result;
            }
            if (result.feasible() && (bestFeasible == null || isBetter(result, bestFeasible))) {
                bestFeasible = result;
            }
        }
        return bestFeasible != null ? bestFeasible : best;
    }

    /** Wraps a solved timetable with its score and the structural counts behind it. */
    public static GenerationResult describe(Timetable solution, long seed) {
        long hard = solution.getScore().hardScore();
        long medium = solution.getScore().mediumScore();
        long soft = solution.getScore().softScore();
        return new GenerationResult(solution, seed, hard == 0 && medium == 0, hard, medium, soft,
                countIdleEvenings(solution), countOffHomeLessons(solution), countHoles(solution));
    }

    /**
     * Evenings a live track leaves empty between its first and last lesson. Mirrors
     * the idle-evening constraint; the taper after a track's last subject is free
     * and therefore not counted.
     */
    public static long countIdleEvenings(Timetable timetable) {
        Map<Track, int[]> spanByTrack = new HashMap<>();
        for (Block block : timetable.getBlocks()) {
            block.getPlacedLessons().forEach(placed -> {
                int index = placed.slot().getIndex();
                int[] span = spanByTrack.computeIfAbsent(placed.track(),
                        key -> new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE, 0 });
                span[0] = Math.min(span[0], index);
                span[1] = Math.max(span[1], index);
                span[2]++;
            });
        }
        return spanByTrack.values().stream()
                .mapToLong(span -> (long) span[1] - span[0] + 1 - span[2])
                .sum();
    }

    /** Lessons that spilled off their UC's home weekday. */
    public static long countOffHomeLessons(Timetable timetable) {
        return timetable.getBlocks().stream()
                .mapToLong(Block::getOverflowCount)
                .sum();
    }

    /** Evenings a run skips because the teacher is unavailable that date. */
    public static long countHoles(Timetable timetable) {
        return timetable.getBlocks().stream()
                .mapToLong(Block::getHoles)
                .sum();
    }

    private static boolean isBetter(GenerationResult a, GenerationResult b) {
        return a.timetable().getScore().compareTo(b.timetable().getScore()) > 0;
    }
}
