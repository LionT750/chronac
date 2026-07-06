package org.acme.schooltimetabling;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Timetable;
import org.acme.schooltimetabling.solver.TimetableConstraintProvider;
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
                .withTerminationSpentLimit(Duration.ofSeconds(30)));

        // Load the problem
        Timetable problem = new Timetable.Builder(LocalDate.of(2026, 3, 23), LocalDate.of(2026, 8, 7))
                .withName("Demo Timetable")             
                .withRooms(List.of(
                        new Room(Long.toString(1L), "Sala 114")))
                .build();

        // Solve the problem
        Solver<Timetable> solver = solverFactory.buildSolver();
        Timetable solution = solver.solve(problem);

        // Visualize the solution
        printTimetable(solution);
    }


    private static void printTimetable(Timetable timetable) {
    LOGGER.info("========== SOLUTION SUMMARY ==========");

    List<Lesson> lessons = timetable.getLessons();

    long total = lessons.size();
    long assigned = lessons.stream()
            .filter(l -> l.getTimeslot() != null && l.getRoom() != null)
            .count();
    long unassigned = total - assigned;

    LOGGER.info("Total lessons     : {}", total);
    LOGGER.info("Assigned lessons  : {}", assigned);
    LOGGER.info("Unassigned lessons: {}", unassigned);

    // Group by subject for visibility
    Map<String, Long> bySubject = lessons.stream()
            .collect(Collectors.groupingBy(
                    Lesson::getSubject,
                    Collectors.counting()
            ));

    LOGGER.info("---------- Distribution by subject ----------");
    bySubject.forEach((subject, count) ->
            LOGGER.info("Subject: {} -> {} lessons", subject, count)
    );

    // Assignment details (what optimizer actually did)
    LOGGER.info("---------- Assigned lessons ----------");

    lessons.stream()
            .filter(l -> l.getTimeslot() != null && l.getRoom() != null)
            .sorted((a, b) -> {
                int cmp = a.getTimeslot().getDate().compareTo(b.getTimeslot().getDate());
                if (cmp != 0) return cmp;
                return a.getTimeslot().getStartTime().compareTo(b.getTimeslot().getStartTime());
            })
            .forEach(l -> LOGGER.info(
                    "{} | teacher={} | room={} | {} {} - {}",
                    l.getSubject(),
                    l.getTeacher(),
                    l.getRoom().getName(),
                    l.getTimeslot().getDate(),
                    l.getTimeslot().getStartTime(),
                    l.getTimeslot().getEndTime()
            ));

    // Unassigned breakdown (very important for optimizer debugging)
    List<Lesson> unassignedLessons = lessons.stream()
            .filter(l -> l.getTimeslot() == null || l.getRoom() == null)
            .toList();

    if (!unassignedLessons.isEmpty()) {
        LOGGER.info("---------- UNASSIGNED LESSONS ----------");

        Map<String, Long> unassignedBySubject = unassignedLessons.stream()
                .collect(Collectors.groupingBy(Lesson::getSubject, Collectors.counting()));

        unassignedBySubject.forEach((subject, count) ->
                LOGGER.warn("Unassigned subject: {} -> {}", subject, count)
        );

        unassignedLessons.forEach(l ->
                LOGGER.warn("UNASSIGNED | subject={} | teacher={}",
                        l.getSubject(), l.getTeacher())
        );
    }

    // Optional: timeslot utilization insight
    LOGGER.info("---------- TIMESLOT USAGE ----------");

    Map<Timeslot, Map<String, Long>> usage = lessons.stream()
        .filter(l -> l.getTimeslot() != null && l.getTeacher() != null)
        .collect(Collectors.groupingBy(
                Lesson::getTimeslot,
                Collectors.groupingBy(
                        Lesson::getTeacher,
                        Collectors.counting()
                )
        ));

    usage.entrySet().stream()
        .sorted((a, b) -> {
            int cmp = a.getKey().getDate().compareTo(b.getKey().getDate());
            if (cmp != 0) return cmp;
            return a.getKey().getStartTime().compareTo(b.getKey().getStartTime());
        })
        .forEach(e -> {
            Timeslot t = e.getKey();
            String dayOfWeek = t.getDayOfWeek().toString(); // adjust if enum/string differs

            e.getValue().forEach((teacher, count) -> {
                LOGGER.info(
                        "{} {} ({}) -> {} | teacher={} | {} lessons",
                        t.getDate(),
                        t.getStartTime(),
                        dayOfWeek,
                        teacher,
                        count
                );
            });
        });

    LOGGER.info("========== END SOLUTION ==========");
    }   
}
