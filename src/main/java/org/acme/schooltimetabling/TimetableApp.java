package org.acme.schooltimetabling;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Timetable;
import org.acme.schooltimetabling.solver.TimetableConstraintProvider;
import org.acme.schooltimetabling.rest.TimetableHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TimetableApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableApp.class);


    public static void main(String[] args) {
        SolverFactory<Timetable> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(Timetable.class)
                .withEntityClasses(Lesson.class)
                .withConstraintProviderClass(TimetableConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(1 * 60)));

        // Load the problem
        Timetable problem = new Timetable.Builder(LocalDate.of(2026, 7, 23), LocalDate.of(2027, 3, 8))
                .withName("MultiTurma Demo")             
                .withRooms(List.of(
                        new Room(Long.toString(1L), "Sala 114"),
                        new Room(Long.toString(2L), "Sala 115")))
                .build();

        // Solve the problem
        Solver<Timetable> solver = solverFactory.buildSolver();
        Timetable solution = solver.solve(problem);

        // Check for infeasibility
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

        // Visualize the solution
        printTimetable(solution);

        // Expose the solution via GET /api/timetable
        try {
            new TimetableHttpServer(solution).start(8080);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to start REST API.", e);
        }
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
