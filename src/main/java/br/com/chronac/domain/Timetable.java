package br.com.chronac.domain;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@PlanningSolution
public class Timetable {

    private static final LocalTime LESSON_START = LocalTime.of(18, 40);
    private static final LocalTime LESSON_END = LocalTime.of(22, 0);

    private String name;

    @ProblemFactCollectionProperty
    private List<Turma> turmas;

    @ProblemFactCollectionProperty
    private List<Track> tracks;

    @ProblemFactCollectionProperty
    private List<Slot> slots;

    @ProblemFactCollectionProperty
    private List<SubjectPart> parts;

    @ProblemFactCollectionProperty
    private List<Room> rooms;

    @ProblemFactCollectionProperty
    private List<TeacherSchedule> teacherSchedules;

    @ProblemFactCollectionProperty
    private List<Timeslot> timeslots;

    @ProblemFactCollectionProperty
    private List<Week> weeks;

    @PlanningEntityCollectionProperty
    private List<Block> blocks;

    @PlanningScore
    private HardMediumSoftScore score;

    /**
     * Lets the constraint weights be retuned without recompiling - the defaults
     * live in TimetableConstraintProvider and none() means "use them as declared".
     */
    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides =
            ConstraintWeightOverrides.none();

    /** Room per turma name, used to materialize lessons. */
    private Map<String, Room> roomByTurma = Map.of();

    /** One Timeslot per class date, reused when materializing lessons. */
    private Map<LocalDate, Timeslot> timeslotByDate = Map.of();

    // Required by Timefold
    public Timetable() {
        this.turmas = List.of();
        this.tracks = List.of();
        this.slots = List.of();
        this.parts = List.of();
        this.rooms = List.of();
        this.teacherSchedules = List.of();
        this.timeslots = List.of();
        this.weeks = List.of();
        this.blocks = List.of();
    }

    private Timetable(Builder builder) {
        this.name = builder.name;
        this.turmas = List.copyOf(builder.turmas);
        this.tracks = List.copyOf(builder.tracks);
        this.slots = List.copyOf(builder.slots);
        this.parts = List.copyOf(builder.parts);
        this.rooms = List.copyOf(builder.rooms);
        this.teacherSchedules = List.copyOf(builder.teacherSchedules);
        this.timeslots = List.copyOf(builder.timeslots);
        this.weeks = List.copyOf(builder.weeks);
        this.blocks = List.copyOf(builder.blocks);
        this.roomByTurma = Map.copyOf(builder.roomByTurma);
        this.timeslotByDate = Map.copyOf(builder.timeslotByDate);
        if (builder.constraintWeights != null) {
            this.constraintWeightOverrides = ConstraintWeightOverrides.of(builder.constraintWeights);
        }
        this.score = null;
    }

    public static class Builder {

        private final Semester semester;

        private String name;
        private List<Room> rooms;
        private List<TeacherSchedule> teacherSchedules = List.of();
        private Map<String, HardMediumSoftScore> constraintWeights;

        private List<Turma> turmas = List.of();
        private List<Track> tracks = List.of();
        private List<Slot> slots = List.of();
        private List<SubjectPart> parts = List.of();
        private List<Block> blocks = List.of();
        private List<Timeslot> timeslots = List.of();
        private List<Week> weeks = List.of();
        private Map<String, Room> roomByTurma = Map.of();
        private Map<LocalDate, Timeslot> timeslotByDate = Map.of();

        public Builder(LocalDate semesterStartDate, LocalDate semesterEndDate) {
            this.semester = new Semester(
                    semesterStartDate,
                    semesterEndDate,
                    Collections.emptyList()
            );
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withRooms(List<Room> rooms) {
            this.rooms = new ArrayList<>(rooms);
            return this;
        }

        public Builder withTeacherSchedules(List<TeacherSchedule> teacherSchedules) {
            this.teacherSchedules = new ArrayList<>(teacherSchedules);
            return this;
        }

        /** Overrides constraint weights by constraint name; leave unset to use the declared defaults. */
        public Builder withConstraintWeights(Map<String, HardMediumSoftScore> constraintWeights) {
            this.constraintWeights = constraintWeights == null ? null : new LinkedHashMap<>(constraintWeights);
            return this;
        }

        private void createTimeslots() {
            List<Timeslot> generated = new ArrayList<>();
            Map<LocalDate, Timeslot> byDate = new LinkedHashMap<>();
            long nextId = 0L;
            for (LocalDate validDay : semester.getValidClassDays()) {
                Timeslot timeslot = new Timeslot(
                        Long.toString(nextId++),
                        validDay,
                        validDay.getDayOfWeek(),
                        LESSON_START,
                        LESSON_END);
                generated.add(timeslot);
                byDate.put(validDay, timeslot);
            }
            this.timeslots = generated;
            this.timeslotByDate = byDate;
        }

        private void createWeeks() {
            this.weeks = semester.getValidClassDays()
                    .stream()
                    .map(date -> (long) date.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR))
                    .distinct()
                    .sorted()
                    .map(Week::new)
                    .toList();
        }

        /**
         * Builds one track per (turma, weekday) the turma actually uses. A turma's
         * window is the span of its UCs clamped to the semester, and its weekdays are
         * the union of what its UCs allow - Jovem Programador gets three tracks
         * (Wed/Thu/Fri), Tecnico gets five (Mon-Fri).
         */
        private void createTracks() {
            this.turmas = List.copyOf(semester.getCurriculum().getTurmas());
            List<Track> builtTracks = new ArrayList<>();
            List<Slot> builtSlots = new ArrayList<>();
            Map<String, Room> roomLookup = new LinkedHashMap<>();
            Map<String, Room> roomByName = new HashMap<>();
            for (Room room : rooms) {
                roomByName.put(room.getName(), room);
            }

            for (Turma turma : turmas) {
                LocalDate windowStart = semester.getEndDate();
                LocalDate windowEnd = semester.getStartDate();
                Set<DayOfWeek> weekdays = new LinkedHashSet<>();
                for (Subject subject : turma.getSubjects()) {
                    LocalDate start = subject.getStartDate() == null || subject.getStartDate().isBefore(semester.getStartDate())
                            ? semester.getStartDate()
                            : subject.getStartDate();
                    LocalDate end = subject.getEndDate() == null || subject.getEndDate().isAfter(semester.getEndDate())
                            ? semester.getEndDate()
                            : subject.getEndDate();
                    if (start.isBefore(windowStart)) {
                        windowStart = start;
                    }
                    if (end.isAfter(windowEnd)) {
                        windowEnd = end;
                    }
                    weekdays.addAll(subject.getEffectiveDayOfWeeks());
                }

                for (DayOfWeek weekday : DayOfWeek.values()) {
                    if (!weekdays.contains(weekday)) {
                        continue;
                    }
                    List<LocalDate> dates = new ArrayList<>();
                    for (LocalDate day : semester.getValidClassDays()) {
                        if (day.getDayOfWeek() == weekday && !day.isBefore(windowStart) && !day.isAfter(windowEnd)) {
                            dates.add(day);
                        }
                    }
                    if (dates.isEmpty()) {
                        continue;
                    }
                    Track track = new Track(turma, weekday, dates);
                    builtTracks.add(track);
                    builtSlots.addAll(track.getSlots());
                }

                if (!turma.getRooms().isEmpty()) {
                    Room room = roomByName.get(turma.getRooms().get(0));
                    if (room != null) {
                        roomLookup.put(turma.getName(), room);
                    }
                }
            }

            this.tracks = builtTracks;
            this.slots = builtSlots;
            this.roomByTurma = roomLookup;
        }

        /**
         * The first week where every teacher of the turma could hold an evening: the
         * earliest week offering at least as many class days as the turma has
         * teachers. Jovem Programador opens Thursday 23 July with two evenings for
         * three teachers, so its opening week is the following one.
         */
        private void computeFirstViableWeeks() {
            for (Turma turma : turmas) {
                Map<Long, Set<DayOfWeek>> daysPerWeek = new java.util.TreeMap<>();
                for (Track track : tracks) {
                    if (track.getTurma() != turma) {
                        continue;
                    }
                    for (Slot slot : track.getSlots()) {
                        daysPerWeek.computeIfAbsent(slot.getWeekIndex(), key -> new LinkedHashSet<>())
                                .add(track.getDayOfWeek());
                    }
                }
                int required = turma.getTeacherCount();
                long firstViable = daysPerWeek.keySet().stream().findFirst().orElse(0L);
                for (Map.Entry<Long, Set<DayOfWeek>> entry : daysPerWeek.entrySet()) {
                    if (entry.getValue().size() >= required) {
                        firstViable = entry.getKey();
                        break;
                    }
                }
                turma.setFirstViableWeek(firstViable);
            }
        }

        /**
         * One block per subject part, each carrying the evenings it could legally
         * use: the turma's tracks on weekdays the UC allows, inside the UC's date
         * window, on dates this part's teacher is available. Illegal placements are
         * therefore unreachable rather than penalized.
         */
        private void createBlocks() {
            List<SubjectPart> builtParts = new ArrayList<>();
            List<Block> builtBlocks = new ArrayList<>();

            for (Turma turma : turmas) {
                for (Subject subject : turma.getSubjects()) {
                    LocalDate start = subject.getStartDate();
                    LocalDate end = subject.getEndDate() == null ? semester.getEndDate() : subject.getEndDate();
                    for (SubjectPart part : subject.getParts()) {
                        TeacherSchedule schedule = semester.getCurriculum().getTeacherSchedule(part.getTeacher());
                        Set<LocalDate> unavailable = schedule == null
                                ? Set.of()
                                : new TreeSet<>(schedule.getSpecificUnavailableDates());

                        List<Slot> allowed = new ArrayList<>();
                        for (Track track : tracks) {
                            if (track.getTurma() != turma
                                    || !subject.getEffectiveDayOfWeeks().contains(track.getDayOfWeek())) {
                                continue;
                            }
                            for (Slot slot : track.getSlots()) {
                                LocalDate date = slot.getDate();
                                if (start != null && date.isBefore(start)) {
                                    continue;
                                }
                                if (date.isAfter(end) || unavailable.contains(date)) {
                                    continue;
                                }
                                allowed.add(slot);
                            }
                        }

                        builtParts.add(part);
                        builtBlocks.add(new Block(part, allowed, unavailable));
                    }
                }
            }

            this.parts = builtParts;
            this.blocks = builtBlocks;
        }

        public Timetable build() {
            Objects.requireNonNull(name, "Name must be provided.");
            Objects.requireNonNull(rooms, "Rooms must be provided.");

            // Register teacher schedules with curriculum
            for (TeacherSchedule schedule : teacherSchedules) {
                semester.getCurriculum().registerTeacherSchedule(schedule);
            }
            semester.getCurriculum().applyTeacherSchedules();

            // Generate derived data automatically.
            createTimeslots();
            createWeeks();
            createTracks();
            computeFirstViableWeeks();
            createBlocks();

            return new Timetable(this);
        }
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

    @JsonIgnore
    public List<Turma> getTurmas() {
        return turmas;
    }

    @JsonIgnore
    public List<Track> getTracks() {
        return tracks;
    }

    @JsonIgnore
    public List<Slot> getSlots() {
        return slots;
    }

    @JsonIgnore
    public List<SubjectPart> getParts() {
        return parts;
    }

    @JsonIgnore
    public List<Block> getBlocks() {
        return blocks;
    }

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Week> getWeeks() {
        return weeks;
    }

    public List<TeacherSchedule> getTeacherSchedules() {
        return teacherSchedules;
    }

    /**
     * The flat list of dated lessons, materialized from the blocks. Blocks are what
     * the solver plans; this is what the API and the UI consume, so the payload
     * shape is unchanged from when Lesson was the planning entity.
     */
    public List<Lesson> getLessons() {
        List<Lesson> materialized = new ArrayList<>();
        long nextId = 0L;
        for (Block block : blocks) {
            Room room = roomByTurma.get(block.getPart().getSubject().getTurma().getName());
            for (PlacedLesson placed : block.getPlacedLessons()) {
                materialized.add(new Lesson(
                        Long.toString(nextId++),
                        block.getPart(),
                        timeslotByDate.get(placed.date()),
                        room));
            }
        }
        return materialized;
    }

    @JsonIgnore
    public ConstraintWeightOverrides<HardMediumSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(
            ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }
}
