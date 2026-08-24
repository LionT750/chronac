package br.com.chronac.service;

import br.com.chronac.demo.TimetableDemoData;
import br.com.chronac.domain.Lesson;
import br.com.chronac.domain.Timeslot;
import br.com.chronac.domain.Timetable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Solves the demo timetable when the application starts and caches the best
 * solution found so far, so GET /api/timetable can serve it instantly.
 */
@Service
public class TimetableDemoSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableDemoSolver.class);

    private final TimetableGenerator timetableGenerator;
    private final long demoSeed;
    private final long demoSeconds;
    private final long demoUnimprovedSeconds;
    private final Object lock = new Object();
    private Timetable problem;
    private Timetable bestSolution;

    public TimetableDemoSolver(TimetableGenerator timetableGenerator,
            @Value("${chronac.demo.seed:" + TimetableGenerator.DEMO_SEED + "}") long demoSeed,
            @Value("${chronac.demo.seconds:" + TimetableGenerator.DEMO_BUDGET_SECONDS + "}") long demoSeconds,
            @Value("${chronac.demo.unimproved-seconds:"
                    + TimetableGenerator.DEMO_UNIMPROVED_SECONDS + "}") long demoUnimprovedSeconds) {
        this.timetableGenerator = timetableGenerator;
        this.demoSeed = demoSeed;
        this.demoSeconds = demoSeconds;
        this.demoUnimprovedSeconds = demoUnimprovedSeconds;
    }

    /**
     * Solves the demo with the pinned seed, so GET /api/timetable serves the very
     * same timetable on every restart. A pitch needs a schedule you can talk
     * through, not whatever the last run happened to find.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void solveDemoOnStartup() {
        synchronized (lock) {
            problem = TimetableDemoData.buildDemoProblem();
        }

        CompletableFuture.runAsync(() -> {
            Timetable finalSolution;
            try {
                finalSolution = timetableGenerator.solveSeeded(
                        TimetableDemoData.buildDemoProblem(), demoSeed, demoSeconds, demoUnimprovedSeconds);
            } catch (RuntimeException e) {
                LOGGER.error("Demo solve failed.", e);
                return;
            }
            synchronized (lock) {
                this.bestSolution = finalSolution;
            }
            reportFinalSolution(finalSolution);
        });
    }

    public Timetable getBestSolution() {
        synchronized (lock) {
            if (bestSolution != null) {
                return bestSolution;
            }
            if (problem == null) {
                problem = TimetableDemoData.buildDemoProblem();
            }
            return problem;
        }
    }

    private void reportFinalSolution(Timetable solution) {
        LOGGER.info("Demo solved with pinned seed {} (budget {}s, stops after {}s without progress).",
                demoSeed, demoSeconds, demoUnimprovedSeconds);
        report(solution);
    }

    private static void report(Timetable solution) {
        if (solution.getScore() != null
                && (solution.getScore().hardScore() < 0 || solution.getScore().mediumScore() < 0)) {
            LOGGER.warn("========== INFEASIBLE SCHEDULE ==========");
            LOGGER.warn("The solver could not satisfy all hard/medium constraints.");
            LOGGER.warn("Score: {}", solution.getScore());
            LOGGER.warn("This means no valid timetable exists for the given input data.");
            LOGGER.warn("Consider: adding more rooms/timeslots, reducing lesson count, or relaxing constraints.");
            LOGGER.warn("=========================================");
        } else {
            LOGGER.info("Schedule is feasible. Score: {} ({} idle evenings, {} spilled lessons, {} absence holes)",
                    solution.getScore(),
                    TimetableGenerator.countIdleEvenings(solution),
                    TimetableGenerator.countOffHomeLessons(solution),
                    TimetableGenerator.countHoles(solution));
        }

        printTimetable(solution);
    }

    private static void printTimetable(Timetable timetable) {
        LOGGER.info("========== TIMETABLE ==========");

        List<Lesson> lessons = timetable.getLessons();
        List<Timeslot> allTimeslots = timetable.getTimeslots();

        // Map each timeslot to all lessons assigned to it.
        Map<Timeslot, String> lessonsPerTimeslot = lessons.stream()
                .filter(l -> l.getTimeslot() != null)
                .collect(Collectors.groupingBy(
                        Lesson::getTimeslot,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparing(Lesson::getTeacher))
                                        .map(l -> l.getTeacher() + " (" + l.getSubject().getName() + ")")
                                        .collect(Collectors.joining(" | ")))));

        WeekFields weekFields = WeekFields.ISO;

        // Group by ISO week-based year + week number.
        Map<String, List<Timeslot>> weeks = allTimeslots.stream()
                .collect(Collectors.groupingBy(
                        t -> String.format(
                                "%d-W%02d",
                                t.getDate().get(weekFields.weekBasedYear()),
                                t.getDate().get(weekFields.weekOfWeekBasedYear()))));

        weeks.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String week = entry.getKey();
                    List<Timeslot> weekSlots = entry.getValue();

                    LOGGER.info("");
                    LOGGER.info("=========== {} ===========", week);

                    // Map: startTime -> (dayOfWeek -> timeslot)
                    Map<LocalTime, Map<DayOfWeek, Timeslot>> grid = weekSlots.stream()
                            .collect(Collectors.groupingBy(
                                    Timeslot::getStartTime,
                                    Collectors.toMap(
                                            Timeslot::getDayOfWeek,
                                            t -> t)));

                    List<LocalTime> startTimes = grid.keySet().stream()
                            .sorted()
                            .toList();

                    LOGGER.info(String.format(
                            "%-8s %-45s %-45s %-45s %-45s %-45s",
                            "Time",
                            "MONDAY",
                            "TUESDAY",
                            "WEDNESDAY",
                            "THURSDAY",
                            "FRIDAY"));

                    for (LocalTime startTime : startTimes) {
                        StringBuilder row = new StringBuilder();
                        row.append(String.format("%-8s", startTime));

                        for (DayOfWeek day : List.of(
                                DayOfWeek.MONDAY,
                                DayOfWeek.TUESDAY,
                                DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY,
                                DayOfWeek.FRIDAY)) {
                            Timeslot slot = grid.get(startTime).get(day);
                            String cell = "-";
                            if (slot != null) {
                                cell = lessonsPerTimeslot.getOrDefault(slot, "-");
                            }
                            row.append(String.format(" %-45s", cell));
                        }

                        LOGGER.info(row.toString());
                    }
                });

        LOGGER.info("======================================");
    }
}