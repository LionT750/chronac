package br.com.chronac.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftScore;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@PlanningSolution
public class Timetable {

    private static final int HOURS_PER_LESSON = 4;
    private static final LocalTime LESSON_START = LocalTime.of(18, 40);
    private static final LocalTime LESSON_END = LocalTime.of(22, 0);

    private String name;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Timeslot> timeslots;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Room> rooms;

    @ProblemFactCollectionProperty
    private List<Week> weeks;

    @ProblemFactCollectionProperty
    private List<TeacherSchedule> teacherSchedules;

    @PlanningEntityCollectionProperty
    private List<Lesson> lessons;

    @PlanningScore
    private HardSoftScore score;

    // Required by Timefold
    public Timetable() {
        this.timeslots = List.of();
        this.rooms = List.of();
        this.weeks = List.of();
        this.teacherSchedules = List.of();
        this.lessons = List.of();
    }

    private Timetable(Builder builder) {
        this.name = builder.name;
        this.timeslots = List.copyOf(builder.timeslots);
        this.rooms = List.copyOf(builder.rooms);
        this.lessons = List.copyOf(builder.lessons);
        this.weeks = List.copyOf(builder.weeks);
        this.teacherSchedules = List.copyOf(builder.teacherSchedules);
        this.score = null;
    }

    public static class Builder {

        private final Semester semester;

        private String name;
        private List<Timeslot> timeslots;
        private List<Room> rooms;
        private List<Lesson> lessons;
        private List<Week> weeks;
        private List<TeacherSchedule> teacherSchedules = List.of();

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

        private void createTimeslots() {
            List<Timeslot> generatedTimeslots = new ArrayList<>();
            long nextTimeslotId = 0L;

            for (LocalDate validDay : semester.getValidClassDays()) {
                generatedTimeslots.add(
                        new Timeslot(
                                Long.toString(nextTimeslotId++),
                                validDay,
                                validDay.getDayOfWeek(),
                                LESSON_START,
                                LESSON_END
                        )
                );
            }

            this.timeslots = generatedTimeslots;
        }

        private void createLessons() {
            List<Lesson> generatedLessons = new ArrayList<>();
            long nextLessonId = 0L;

            for (Subject subject : semester.getCurriculum().subjects.values()) {

                int lessonCount = subject.getTotalHours() / HOURS_PER_LESSON;

                for (int i = 0; i < lessonCount; i++) {
                    generatedLessons.add(
                            new Lesson(
                                    Long.toString(nextLessonId++),
                                    subject
                            )
                    );
                }
            }

            this.lessons = generatedLessons;
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
            createLessons();

            return new Timetable(this);
        }
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public List<Week> getWeeks() {
        return weeks;
    }

    public List<TeacherSchedule> getTeacherSchedules() {
        return teacherSchedules;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}