package br.com.chronac.service;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import br.com.chronac.demo.TimetableDemoData;
import br.com.chronac.domain.Lesson;
import br.com.chronac.domain.Timeslot;
import br.com.chronac.domain.Timetable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Solves the demo timetable when the application starts and caches the best
 * solution found so far, so GET /api/timetable can serve it instantly.
 */
@Service
public class TimetableDemoSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableDemoSolver.class);
    private static final long DEMO_PROBLEM_ID = 0L;

    private final SolverManager<Timetable> solverManager;
    private final Object lock = new Object();
    private Timetable problem;
    private Timetable bestSolution;

    public TimetableDemoSolver(SolverManager<Timetable> solverManager) {
        this.solverManager = solverManager;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void solveDemoOnStartup() {
        synchronized (lock) {
            problem = TimetableDemoData.buildDemoProblem();
        }

        SolverJob<Timetable> solverJob = solverManager.solveAndListen(DEMO_PROBLEM_ID, problem,
                bestSolution -> {
                    synchronized (lock) {
                        this.bestSolution = bestSolution;
                    }
                });

        CompletableFuture.runAsync(() -> {
            Timetable finalSolution;
            try {
                finalSolution = solverJob.getFinalBestSolution();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException e) {
                LOGGER.error("Demo solve failed.", e.getCause());
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

    private static void reportFinalSolution(Timetable solution) {
        if (solution.getScore() != null && solution.getScore().hardScore() < 0) {
            LOGGER.warn("========== INFEASIBLE SCHEDULE ==========");
            LOGGER.warn("The solver could not satisfy all hard constraints.");
            LOGGER.warn("Score: {}", solution.getScore());
            LOGGER.warn("This means no valid timetable exists for the given input data.");
            LOGGER.warn("Consider: adding more rooms/timeslots, reducing lesson count, or relaxing constraints.");
            LOGGER.warn("=========================================");
        } else {
            LOGGER.info("Schedule is feasible. Score: {}", solution.getScore());
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
                                        .collect(Collectors.joining(" | "))
                        )
                ));

        WeekFields weekFields = WeekFields.ISO;

        // Group by ISO week-based year + week number.
        Map<String, List<Timeslot>> weeks = allTimeslots.stream()
                .collect(Collectors.groupingBy(
                        t -> String.format(
                                "%d-W%02d",
                                t.getDate().get(weekFields.weekBasedYear()),
                                t.getDate().get(weekFields.weekOfWeekBasedYear())
                        )
                ));

        weeks.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String week = entry.getKey();
                    List<Timeslot> weekSlots = entry.getValue();

                    LOGGER.info("");
                    LOGGER.info("=========== {} ===========", week);

                    // Map: startTime -> (dayOfWeek -> timeslot)
                    Map<LocalTime, Map<DayOfWeek, Timeslot>> grid =
                            weekSlots.stream()
                                    .collect(Collectors.groupingBy(
                                            Timeslot::getStartTime,
                                            Collectors.toMap(
                                                    Timeslot::getDayOfWeek,
                                                    t -> t
                                            )
                                    ));

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
                            "FRIDAY"
                    ));

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