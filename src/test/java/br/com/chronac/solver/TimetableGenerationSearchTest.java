package br.com.chronac.solver;

import br.com.chronac.demo.TimetableDemoData;
import br.com.chronac.domain.Block;
import br.com.chronac.domain.PlacedLesson;
import br.com.chronac.domain.Slot;
import br.com.chronac.domain.Subject;
import br.com.chronac.domain.Timetable;
import br.com.chronac.domain.Track;
import br.com.chronac.domain.Turma;
import br.com.chronac.service.TimetableGenerator;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Solves the real demo problem with the seed pinned for the demo, and checks the
 * result is a timetable a school could actually run.
 *
 * The assertions are structural rather than score thresholds: the score is what
 * the solver optimises, so asserting on it only says the solver did what it was
 * told. These say the schedule itself makes sense.
 */
class TimetableGenerationSearchTest {

    @Test
    void pinnedDemoSeedProducesARunnableTimetable() {
        Timetable timetable = new TimetableGenerator()
                .solveSeeded(TimetableDemoData.buildDemoProblem(), TimetableGenerator.DEMO_SEED);

        System.out.println("===== PINNED DEMO (seed " + TimetableGenerator.DEMO_SEED + ") =====");
        System.out.println("Score           : " + timetable.getScore());
        System.out.println("idle evenings   : " + TimetableGenerator.countIdleEvenings(timetable));
        System.out.println("spilled lessons : " + TimetableGenerator.countOffHomeLessons(timetable));
        System.out.println("teacher-absence holes: " + TimetableGenerator.countHoles(timetable));
        printTracks(timetable);

        assertEquals(0, timetable.getScore().hardScore(),
                "The pinned demo seed must be hard-feasible; score was " + timetable.getScore());
        assertEquals(0, timetable.getScore().mediumScore(),
                "The pinned demo seed must not break a school policy; score was " + timetable.getScore());

        // ----- every lesson of every UC is delivered -----
        int expectedLessons = timetable.getParts().stream()
                .mapToInt(part -> part.getLessonCount())
                .sum();
        assertEquals(expectedLessons, timetable.getLessons().size(),
                "Every lesson of every UC must be placed");

        // ----- one evening, one room, one lesson -----
        Map<Slot, Long> perSlot = lessons(timetable).stream()
                .collect(Collectors.groupingBy(PlacedLesson::slot, Collectors.counting()));
        assertTrue(perSlot.values().stream().noneMatch(count -> count > 1),
                "Two lessons landed on the same evening in the same room");

        // ----- a teacher is in one place at a time -----
        Map<String, Long> teacherClashes = lessons(timetable).stream()
                .collect(Collectors.groupingBy(placed -> placed.teacher() + "@" + placed.date(),
                        Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertTrue(teacherClashes.isEmpty(), "Teacher double-booked: " + teacherClashes);

        // ----- every UC owns one weekday, bar its sporadic spill -----
        for (Block block : timetable.getBlocks()) {
            Set<DayOfWeek> homeWeekdays = block.getHomeLessonSlots().stream()
                    .map(Slot::getDayOfWeek)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            assertEquals(1, homeWeekdays.size(),
                    block.getId() + " should own exactly one weekday but used " + homeWeekdays);
            assertTrue(block.getOverflowCount() <= Block.MAX_OVERFLOW,
                    block.getId() + " spilled " + block.getOverflowCount() + " lessons off its weekday");
        }

        // ----- a UC runs as one unbroken block; only a teacher absence may interrupt it -----
        for (Block block : timetable.getBlocks()) {
            Set<Integer> occupied = block.getHomeLessonSlots().stream()
                    .map(Slot::getIndex)
                    .collect(Collectors.toSet());
            for (int index = block.getStartIndex(); index <= block.getEndIndex(); index++) {
                if (!occupied.contains(index)) {
                    LocalDate skipped = block.getTrack().slotAt(index).getDate();
                    assertTrue(block.getTeacherUnavailableDates().contains(skipped),
                            block.getId() + " skips " + skipped
                                    + " in the middle of its run for no reason");
                }
            }
        }

        // ----- both parts of UC11 run back to back on one weekday -----
        for (Subject subject : timetable.getTurmas().stream()
                .flatMap(turma -> turma.getSubjects().stream())
                .filter(candidate -> candidate.getParts().size() > 1)
                .toList()) {
            List<Block> parts = timetable.getBlocks().stream()
                    .filter(block -> block.getPart().getSubject() == subject)
                    .sorted(Comparator.comparingInt(block -> block.getPart().getIndex()))
                    .toList();
            for (int i = 1; i < parts.size(); i++) {
                Block previous = parts.get(i - 1);
                Block next = parts.get(i);
                assertEquals(previous.getTrack(), next.getTrack(),
                        subject.getName() + " is one UC: its parts must share a weekday");
                assertEquals(previous.getEndIndex() + 1, next.getStartIndex(),
                        subject.getName() + " must hand over to its next teacher the following evening");
            }
        }

        // ----- every teacher of a turma is live in that turma's first full week -----
        for (Turma turma : timetable.getTurmas()) {
            Set<String> live = lessons(timetable).stream()
                    .filter(placed -> placed.turma() == turma
                            && placed.weekIndex() == turma.getFirstViableWeek())
                    .map(PlacedLesson::teacher)
                    .collect(Collectors.toSet());
            assertEquals(turma.getDistinctTeachers(), live,
                    turma.getName() + " must open with every one of its teachers holding an evening");
        }

        // ----- a teacher's week never changes shape without a reason -----
        // In this model a teacher's evenings are fixed by which blocks of theirs are
        // running, so their weekly load can only change in a week where one of their
        // blocks starts or ends, or where a spill lands. Anything else would mean the
        // weekly pattern wobbles for no reason, which is what the school complained
        // about in the first place.
        assertTeacherWeeksAreStable(timetable);
    }

    private static void assertTeacherWeeksAreStable(Timetable timetable) {
        Map<String, Set<Long>> transitionWeeks = new LinkedHashMap<>();
        for (Block block : timetable.getBlocks()) {
            Set<Long> weeks = transitionWeeks.computeIfAbsent(block.getPart().getTeacher(),
                    key -> new LinkedHashSet<>());
            List<Slot> home = block.getHomeLessonSlots();
            if (!home.isEmpty()) {
                weeks.add(home.get(0).getWeekIndex());
                weeks.add(home.get(home.size() - 1).getWeekIndex());
            }
            block.getOverflowLessonSlots().forEach(slot -> weeks.add(slot.getWeekIndex()));
        }

        Map<String, Map<Long, Integer>> nightsPerWeek = new LinkedHashMap<>();
        lessons(timetable).forEach(placed -> nightsPerWeek
                .computeIfAbsent(placed.teacher(), key -> new TreeMap<>())
                .merge(placed.weekIndex(), 1, Integer::sum));

        for (Map.Entry<String, Map<Long, Integer>> entry : nightsPerWeek.entrySet()) {
            String teacher = entry.getKey();
            List<Long> weeks = new ArrayList<>(entry.getValue().keySet());
            for (int i = 1; i < weeks.size(); i++) {
                long previous = weeks.get(i - 1);
                long current = weeks.get(i);
                if (current != previous + 1) {
                    continue; // not consecutive weeks, nothing to compare
                }
                int before = entry.getValue().get(previous);
                int after = entry.getValue().get(current);
                if (before == after) {
                    continue;
                }
                Set<Long> transitions = transitionWeeks.getOrDefault(teacher, Set.of());
                assertTrue(transitions.contains(previous) || transitions.contains(current),
                        teacher + " goes from " + before + " to " + after + " evenings between weeks "
                                + previous + " and " + current + " with no UC starting or ending");
            }
        }
    }

    private static List<PlacedLesson> lessons(Timetable timetable) {
        return timetable.getBlocks().stream()
                .flatMap(block -> block.getPlacedLessons().stream())
                .toList();
    }

    /** Renders each turma's weekday lanes so the macroscopic shape is visible. */
    private static void printTracks(Timetable timetable) {
        Map<Slot, PlacedLesson> bySlot = lessons(timetable).stream()
                .collect(Collectors.toMap(PlacedLesson::slot, placed -> placed, (a, b) -> a));
        for (Turma turma : timetable.getTurmas()) {
            System.out.println("\n" + turma.getName());
            for (Track track : timetable.getTracks()) {
                if (track.getTurma() != turma) {
                    continue;
                }
                StringBuilder row = new StringBuilder(String.format("  %-9s ",
                        track.getDayOfWeek().toString().substring(0, 3)));
                for (Slot slot : track.getSlots()) {
                    PlacedLesson placed = bySlot.get(slot);
                    row.append(placed == null ? " ....."
                            : String.format(" %-5s", placed.part().getSubject().getName()
                                    + (placed.offHome() ? "*" : "")));
                }
                System.out.println(row);
            }
        }
        System.out.println("\n  (* = lesson spilled off its UC's home weekday)");
    }
}
