package br.com.chronac.solver;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;
import br.com.chronac.domain.Block;
import br.com.chronac.domain.Room;
import br.com.chronac.domain.Slot;
import br.com.chronac.domain.Subject;
import br.com.chronac.domain.SubjectPart;
import br.com.chronac.domain.TeacherSchedule;
import br.com.chronac.domain.Timetable;
import br.com.chronac.domain.Track;
import br.com.chronac.domain.Turma;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * One test per constraint, on the smallest arrangement that makes it fire.
 *
 * The fixtures build tracks by hand rather than going through the demo data, so a
 * failure points at the rule and not at the calendar.
 */
class TimetableConstraintProviderTest {

    private static final String ALISSON = "Alisson";
    private static final String RODOLFO = "Rodolfo";
    private static final LocalDate FIRST_MONDAY = LocalDate.of(2026, 9, 14);

    private final ConstraintVerifier<TimetableConstraintProvider, Timetable> constraintVerifier =
            ConstraintVerifier.build(new TimetableConstraintProvider(), Timetable.class, Block.class);

    // ************************************************************************
    // HARD
    // ************************************************************************

    @Test
    void overflowStaysSporadic_whenTheSpillIsTooBigToBeAHotfix() {
        Fixture fixture = new Fixture();
        // 8 lessons starting on the last-but-one Monday: 6 of them have nowhere to go
        // on the home weekday, which is 3 more than counts as sporadic.
        Block block = fixture.block("UC", ALISSON, 8);
        block.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 8));

        constraintVerifier.verifyThat(TimetableConstraintProvider::overflowStaysSporadic)
                .given(fixture.facts(block))
                .penalizesBy(3);
    }

    @Test
    void overflowStaysSporadic_whenEverythingFitsItsWeekday() {
        Fixture fixture = new Fixture();
        Block block = fixture.block("UC", ALISSON, 4);
        block.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));

        constraintVerifier.verifyThat(TimetableConstraintProvider::overflowStaysSporadic)
                .given(fixture.facts(block))
                .penalizesBy(0);
    }

    @Test
    void blocksDoNotOverlap_whenTwoRunsShareAStretchOfTheSameWeekday() {
        Fixture fixture = new Fixture();
        Block first = fixture.block("UC1", ALISSON, 5);
        first.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        Block second = fixture.block("UC2", RODOLFO, 3);
        // Starts inside the first run: indexes 3 and 4 are claimed twice.
        second.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 3));

        constraintVerifier.verifyThat(TimetableConstraintProvider::blocksDoNotOverlap)
                .given(fixture.facts(first, second))
                .penalizesBy(2);
    }

    @Test
    void blocksDoNotOverlap_whenOneRunHandsOverToTheNext() {
        Fixture fixture = new Fixture();
        Block first = fixture.block("UC1", ALISSON, 5);
        first.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        Block second = fixture.block("UC2", RODOLFO, 3);
        second.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 5));

        constraintVerifier.verifyThat(TimetableConstraintProvider::blocksDoNotOverlap)
                .given(fixture.facts(first, second))
                .penalizesBy(0);
    }

    @Test
    void teacherSingleBooked_whenTheSameTeacherIsInBothTurmasOnOneEvening() {
        Fixture fixture = new Fixture();
        Block jovem = fixture.block("UC1", ALISSON, 2);
        jovem.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        // Second turma, same weekday, same dates, same teacher.
        Block tecnico = fixture.otherTurmaBlock("UC2", ALISSON, 2);
        tecnico.setStartSlot(fixture.otherSlot(DayOfWeek.MONDAY, 0));

        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherSingleBooked)
                .given(fixture.facts(jovem, tecnico))
                .penalizesBy(2);
    }

    @Test
    void ucPartsChained_whenTheSecondTeacherStartsOnAnotherWeekday() {
        Fixture fixture = new Fixture();
        List<Block> parts = fixture.twoPartBlocks("UC11", ALISSON, 3, RODOLFO, 3);
        parts.get(0).setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        parts.get(1).setStartSlot(fixture.slot(DayOfWeek.TUESDAY, 3));

        constraintVerifier.verifyThat(TimetableConstraintProvider::ucPartsChained)
                .given(fixture.facts(parts.get(0), parts.get(1)))
                .penalizesBy(1);
    }

    @Test
    void ucPartsChained_whenTheSecondTeacherTakesOverTheNextEvening() {
        Fixture fixture = new Fixture();
        List<Block> parts = fixture.twoPartBlocks("UC11", ALISSON, 3, RODOLFO, 3);
        parts.get(0).setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        parts.get(1).setStartSlot(fixture.slot(DayOfWeek.MONDAY, 3));

        constraintVerifier.verifyThat(TimetableConstraintProvider::ucPartsChained)
                .given(fixture.facts(parts.get(0), parts.get(1)))
                .penalizesBy(0);
    }

    @Test
    void teachersLiveInFirstFullWeek_penalizesByHowManyWeeksLateTheTeacherStarts() {
        Fixture fixture = new Fixture();
        Block block = fixture.block("UC", ALISSON, 2);
        // The turma's tracks open in week 0; this teacher only appears in week 3.
        block.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 3));

        constraintVerifier.verifyThat(TimetableConstraintProvider::teachersLiveInFirstFullWeek)
                .given(fixture.facts(block))
                .penalizesBy(3);
    }

    @Test
    void overflowWellPlaced_whenTheSpillHasNowhereToGo() {
        Fixture fixture = new Fixture();
        Block block = fixture.block("UC", ALISSON, 8);
        // 8 lessons from index 5 of a 10-slot track: 3 spill, and no weekday was borrowed.
        block.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 5));

        constraintVerifier.verifyThat(TimetableConstraintProvider::overflowWellPlaced)
                .given(fixture.facts(block))
                .penalizesBy(3);
    }

    @Test
    void overflowWellPlaced_whenTheSpillGoesBackOntoItsOwnWeekday() {
        Fixture fixture = new Fixture();
        Block block = fixture.block("UC", ALISSON, 8);
        block.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 5));
        // Same weekday: that is a gap in the run, not a reposicao. And only one of the
        // three spilled lessons still fits before the track ends.
        block.setOverflowStartSlot(fixture.slot(DayOfWeek.MONDAY, 9));

        constraintVerifier.verifyThat(TimetableConstraintProvider::overflowWellPlaced)
                .given(fixture.facts(block))
                .penalizesBy(3);
    }

    @Test
    void overflowWellPlaced_whenTheSpillLandsOnAnotherWeekdayThatHoldsIt() {
        Fixture fixture = new Fixture();
        Block block = fixture.block("UC", ALISSON, 8);
        block.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 5));
        block.setOverflowStartSlot(fixture.slot(DayOfWeek.TUESDAY, 5));

        constraintVerifier.verifyThat(TimetableConstraintProvider::overflowWellPlaced)
                .given(fixture.facts(block))
                .penalizesBy(0);
    }

    @Test
    void overflowWellPlaced_whenAWeekdayIsBorrowedButNothingSpilled() {
        Fixture fixture = new Fixture();
        Block block = fixture.block("UC", ALISSON, 3);
        block.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        block.setOverflowStartSlot(fixture.slot(DayOfWeek.TUESDAY, 0));

        constraintVerifier.verifyThat(TimetableConstraintProvider::overflowWellPlaced)
                .given(fixture.facts(block))
                .penalizesBy(1);
    }

    // ************************************************************************
    // MEDIUM AND SOFT
    // ************************************************************************

    @Test
    void offHomeMidSemester_whenTheBorrowedEveningIsNowhereNearTheEndOfTheRun() {
        Fixture fixture = new Fixture();
        Block block = fixture.block("UC", ALISSON, 11);
        // Runs Mondays 0..9, so its last evening is week 9; the spill sits in week 0.
        block.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        block.setOverflowStartSlot(fixture.slot(DayOfWeek.TUESDAY, 0));

        constraintVerifier.verifyThat(TimetableConstraintProvider::offHomeMidSemester)
                .given(fixture.facts(block))
                .penalizesBy(1);
    }

    @Test
    void offHomeLate_whenTheBorrowedEveningClosesOutTheRun() {
        Fixture fixture = new Fixture();
        Block block = fixture.block("UC", ALISSON, 11);
        block.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        block.setOverflowStartSlot(fixture.slot(DayOfWeek.TUESDAY, 9));

        constraintVerifier.verifyThat(TimetableConstraintProvider::offHomeLate)
                .given(fixture.facts(block))
                .penalizesBy(1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::offHomeMidSemester)
                .given(fixture.facts(block))
                .penalizesBy(0);
    }

    @Test
    void offHomeDriftsFromRunEnd_chargesAWeekAtATimeForTheDistance() {
        Fixture fixture = new Fixture();
        Block block = fixture.block("UC", ALISSON, 11);
        // Last home evening is Monday of week 9, the spill is Tuesday of week 4. That
        // is 34 days apart, so four whole weeks - the charge counts elapsed weeks
        // between the two dates, not the difference between week numbers.
        block.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        block.setOverflowStartSlot(fixture.slot(DayOfWeek.TUESDAY, 4));

        constraintVerifier.verifyThat(TimetableConstraintProvider::offHomeDriftsFromRunEnd)
                .given(fixture.facts(block))
                .penalizesBy(4);
    }

    @Test
    void idleEveningInsideLiveTrack_countsOnlyTheHolesBetweenFirstAndLastLesson() {
        Fixture fixture = new Fixture();
        Block first = fixture.block("UC1", ALISSON, 2);
        first.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        Block second = fixture.block("UC2", RODOLFO, 2);
        // Leaves indexes 2 and 3 empty in the middle; indexes 6..9 are tail and free.
        second.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 4));

        constraintVerifier.verifyThat(TimetableConstraintProvider::idleEveningInsideLiveTrack)
                .given(fixture.facts(first, second))
                .penalizesBy(2);
    }

    @Test
    void idleEveningInsideLiveTrack_whenARunSkipsATeacherAbsence() {
        Fixture fixture = new Fixture();
        // Away on the second and third Monday: the run stretches and leaves two holes.
        Block block = fixture.blockAway("UC", ALISSON, 5,
                Set.of(FIRST_MONDAY.plusWeeks(1), FIRST_MONDAY.plusWeeks(2)));
        block.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));

        constraintVerifier.verifyThat(TimetableConstraintProvider::idleEveningInsideLiveTrack)
                .given(fixture.facts(block))
                .penalizesBy(2);
    }

    @Test
    void avoidableHandoff_whenANewTeacherTakesOverWhileTheOutgoingOneStillHadWork() {
        Fixture fixture = new Fixture();
        Block outgoing = fixture.block("UC1", ALISSON, 3);
        outgoing.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        Block incoming = fixture.block("UC2", RODOLFO, 3);
        incoming.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 3));
        // Alisson still has a UC in this turma, and it only starts later.
        Block pending = fixture.block("UC3", ALISSON, 2);
        pending.setStartSlot(fixture.slot(DayOfWeek.TUESDAY, 6));

        constraintVerifier.verifyThat(TimetableConstraintProvider::avoidableHandoff)
                .given(fixture.facts(outgoing, incoming, pending))
                .penalizesBy(1);
    }

    @Test
    void avoidableHandoff_whenTheOutgoingTeacherHadNothingLeftToTeach() {
        Fixture fixture = new Fixture();
        Block outgoing = fixture.block("UC1", ALISSON, 3);
        outgoing.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        Block incoming = fixture.block("UC2", RODOLFO, 3);
        incoming.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 3));

        constraintVerifier.verifyThat(TimetableConstraintProvider::avoidableHandoff)
                .given(fixture.facts(outgoing, incoming))
                .penalizesBy(0);
    }

    @Test
    void avoidableHandoff_whenTheSameTeacherChainsTheirNextSubject() {
        Fixture fixture = new Fixture();
        Block outgoing = fixture.block("UC1", ALISSON, 3);
        outgoing.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 0));
        Block incoming = fixture.block("UC2", ALISSON, 3);
        incoming.setStartSlot(fixture.slot(DayOfWeek.MONDAY, 3));

        constraintVerifier.verifyThat(TimetableConstraintProvider::avoidableHandoff)
                .given(fixture.facts(outgoing, incoming))
                .penalizesBy(0);
    }

    // ************************************************************************
    // Fixture: two turmas, Monday and Tuesday tracks of 10 evenings each
    // ************************************************************************

    private static final class Fixture {

        private final Turma turma = new Turma("Turma A", List.of("Sala 114"), List.of());
        private final Turma otherTurma = new Turma("Turma B", List.of("Sala 115"), List.of());
        private final List<Track> tracks = new ArrayList<>();
        private final List<Object> extraFacts = new ArrayList<>();

        Fixture() {
            turma.setFirstViableWeek(weekIndex(FIRST_MONDAY));
            otherTurma.setFirstViableWeek(weekIndex(FIRST_MONDAY));
            for (Turma owner : List.of(turma, otherTurma)) {
                for (DayOfWeek weekday : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)) {
                    tracks.add(new Track(owner, weekday, dates(weekday)));
                }
            }
            extraFacts.add(new Room("1", "Sala 114"));
            extraFacts.add(new Room("2", "Sala 115"));
            extraFacts.add(new TeacherSchedule(ALISSON));
            extraFacts.add(new TeacherSchedule(RODOLFO));
        }

        private static List<LocalDate> dates(DayOfWeek weekday) {
            LocalDate first = FIRST_MONDAY.plusDays(weekday.getValue() - 1L);
            List<LocalDate> dates = new ArrayList<>();
            for (int week = 0; week < 10; week++) {
                dates.add(first.plusWeeks(week));
            }
            return dates;
        }

        private static long weekIndex(LocalDate date) {
            return date.toEpochDay() / 7;
        }

        Track track(Turma owner, DayOfWeek weekday) {
            return tracks.stream()
                    .filter(candidate -> candidate.getTurma() == owner && candidate.getDayOfWeek() == weekday)
                    .findFirst()
                    .orElseThrow();
        }

        Slot slot(DayOfWeek weekday, int index) {
            return track(turma, weekday).slotAt(index);
        }

        Slot otherSlot(DayOfWeek weekday, int index) {
            return track(otherTurma, weekday).slotAt(index);
        }

        Block block(String name, String teacher, int lessons) {
            return blockAway(name, teacher, lessons, Set.of());
        }

        Block blockAway(String name, String teacher, int lessons, Set<LocalDate> away) {
            return newBlock(turma, name, teacher, lessons, away);
        }

        Block otherTurmaBlock(String name, String teacher, int lessons) {
            return newBlock(otherTurma, name, teacher, lessons, Set.of());
        }

        private Block newBlock(Turma owner, String name, String teacher, int lessons, Set<LocalDate> away) {
            Subject subject = new Subject(name, FIRST_MONDAY, null, owner.getRooms(),
                    List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));
            subject.setTurma(owner);
            subject.addPart(teacher, lessons * SubjectPart.HOURS_PER_LESSON);
            SubjectPart part = subject.getParts().get(0);
            extraFacts.add(part);
            return new Block(part, allowedSlots(owner), away);
        }

        /** Both parts of one UC, so the chaining rule has something to bind. */
        List<Block> twoPartBlocks(String name, String firstTeacher, int firstLessons,
                String secondTeacher, int secondLessons) {
            Subject subject = new Subject(name, FIRST_MONDAY, null, turma.getRooms(),
                    List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));
            subject.setTurma(turma);
            subject.addPart(firstTeacher, firstLessons * SubjectPart.HOURS_PER_LESSON);
            subject.addPart(secondTeacher, secondLessons * SubjectPart.HOURS_PER_LESSON);
            List<Block> blocks = new ArrayList<>();
            for (SubjectPart part : subject.getParts()) {
                extraFacts.add(part);
                blocks.add(new Block(part, allowedSlots(turma), Set.of()));
            }
            return blocks;
        }

        private List<Slot> allowedSlots(Turma owner) {
            List<Slot> slots = new ArrayList<>();
            for (Track track : tracks) {
                if (track.getTurma() == owner) {
                    slots.addAll(track.getSlots());
                }
            }
            return slots;
        }

        /** Everything the constraint streams need to see, blocks included. */
        Object[] facts(Block... blocks) {
            List<Object> facts = new ArrayList<>(extraFacts);
            facts.addAll(tracks);
            facts.add(turma);
            facts.add(otherTurma);
            facts.addAll(List.of(blocks));
            return facts.toArray();
        }
    }
}
