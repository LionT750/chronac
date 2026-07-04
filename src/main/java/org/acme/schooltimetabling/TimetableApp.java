package org.acme.schooltimetabling;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Timetable;
import org.acme.schooltimetabling.solver.TimetableConstraintProvider;
import org.acme.schooltimetabling.domain.Semester;
import org.acme.schooltimetabling.domain.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TimetableApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableApp.class);

    public enum DemoData {
        SMALL,
        LARGE
    }

    public static void main(String[] args) {
        SolverFactory<Timetable> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(Timetable.class)
                .withEntityClasses(Lesson.class)
                .withConstraintProviderClass(TimetableConstraintProvider.class)
                // The solver runs only for 5 seconds on this small dataset.
                // It's recommended to run for at least 5 minutes ("5m") otherwise.
                .withTerminationSpentLimit(Duration.ofSeconds(30)));

        // Load the problem
        Timetable problem = generateDemoData(DemoData.SMALL);

        // Solve the problem
        Solver<Timetable> solver = solverFactory.buildSolver();
        Timetable solution = solver.solve(problem);

        // Visualize the solution
        printTimetable(solution);
    }

    public static Timetable generateDemoData(DemoData demoData) {
        Semester semester = new Semester(
                java.time.LocalDate.of(2026, 3, 22),
                java.time.LocalDate.of(2026, 8, 7),
                Collections.emptyList());
        List<Timeslot> timeslots = new ArrayList<>();
        long nextTimeslotId = 0L;

        for (LocalDate validDay : semester.getValidClassDays()) {
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), validDay, validDay.getDayOfWeek(), LocalTime.of(18, 40), LocalTime.of(22, 00)));
        }

        List<Room> rooms = new ArrayList<>(1);
        long nextRoomId = 0L;
        rooms.add(new Room(Long.toString(nextRoomId++), "Sala 114"));
    
        List<Lesson> lessons = new ArrayList<>();
        long nextLessonId = 0L;
        
        for (Map.Entry<String, Subject> entry : semester.getCurriculum().subjects.entrySet()) {
            String subject = entry.getKey();
            Subject subjectObject = entry.getValue();
            int hours = subjectObject.getTotalHours();
            int lessonsCount = hours / 4; // Each lesson is of a specific duration
            for (int i = 0; i < lessonsCount; i++) {
                lessons.add(new Lesson(Long.toString(nextLessonId++), subject, subjectObject.getTeacher(), null, null));
            }
        }
        return new Timetable(demoData.name(), timeslots, rooms, lessons);
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
