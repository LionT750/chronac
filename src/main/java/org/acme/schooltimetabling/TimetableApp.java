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

import java.time.Duration;
import java.time.LocalDate;
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
                // The solver runs only for 5 seconds on this small dataset.
                // It's recommended to run for at least 5 minutes ("5m") otherwise.
                .withTerminationSpentLimit(Duration.ofSeconds(60)));

        // Load the problem
        Timetable problem = new Timetable.Builder(LocalDate.of(2026, 3, 23), LocalDate.of(2026, 8, 8))
                .withName("Demo Timetable")             
                .withRooms(List.of(
                        new Room(Long.toString(1L), "Sala 114")))
                .build();

        // Solve the problem
        Solver<Timetable> solver = solverFactory.buildSolver();
        Timetable solution = solver.solve(problem);

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

    // Map each timeslot to the teacher(s) assigned to it.
    Map<Timeslot, String> teachersPerTimeslot = lessons.stream()
            .filter(l -> l.getTimeslot() != null)
            .collect(Collectors.groupingBy(
                    Lesson::getTimeslot,
                    Collectors.mapping(
                            Lesson::getTeacher,
                            Collectors.collectingAndThen(
                                    Collectors.toCollection(java.util.TreeSet::new),
                                    teachers -> String.join(", ", teachers)
                            )
                    )
            ));

    java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO;

    // Group all timeslots by ISO week number.
    Map<Integer, List<Timeslot>> weeks = allTimeslots.stream()
            .collect(Collectors.groupingBy(
                    t -> t.getDate().get(weekFields.weekOfWeekBasedYear())
            ));

    weeks.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {

                Integer week = entry.getKey();
                List<Timeslot> weekSlots = entry.getValue();

                LOGGER.info("");
                LOGGER.info("=========== WEEK {} ===========", week);

                // Map: startTime -> (dayOfWeek -> timeslot)
                Map<java.time.LocalTime, Map<java.time.DayOfWeek, Timeslot>> grid =
                        weekSlots.stream()
                                .collect(Collectors.groupingBy(
                                        Timeslot::getStartTime,
                                        Collectors.toMap(
                                                Timeslot::getDayOfWeek,
                                                t -> t
                                        )
                                ));

                List<java.time.LocalTime> startTimes = grid.keySet().stream()
                        .sorted()
                        .toList();

                LOGGER.info(String.format(
                        "%-8s %-15s %-15s %-15s %-15s %-15s",
                        "Time",
                        "MONDAY",
                        "TUESDAY",
                        "WEDNESDAY",
                        "THURSDAY",
                        "FRIDAY"
                ));

                for (java.time.LocalTime startTime : startTimes) {

                    StringBuilder row = new StringBuilder();
                    row.append(String.format("%-8s", startTime));

                    for (java.time.DayOfWeek day : List.of(
                            java.time.DayOfWeek.MONDAY,
                            java.time.DayOfWeek.TUESDAY,
                            java.time.DayOfWeek.WEDNESDAY,
                            java.time.DayOfWeek.THURSDAY,
                            java.time.DayOfWeek.FRIDAY)) {

                        Timeslot slot = grid.get(startTime).get(day);

                        String teachers = "-";
                        if (slot != null) {
                            teachers = teachersPerTimeslot.getOrDefault(slot, "-");
                        }

                        row.append(String.format(" %-15s", teachers));
                    }

                    LOGGER.info(row.toString());
                }
            });

        LOGGER.info("======================================");
        }
}
