package br.com.chronac.demo;

import br.com.chronac.domain.Teacher;
import br.com.chronac.repository.TeacherRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Seeds the H2 database with the demo teachers on startup, so the persisted
 * Teacher table backs the exact same "MultiTurma Demo" schedule restrictions
 * that {@link TimetableDemoData} used to build inline. Runs as an
 * ApplicationRunner so it completes before TimetableDemoSolver's
 * ApplicationReadyEvent listener solves the demo problem.
 */
@Component
public class TeacherDataSeeder implements ApplicationRunner {

    private final TeacherRepository teacherRepository;

    public TeacherDataSeeder(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (teacherRepository.count() > 0) {
            return;
        }

        Teacher nelma = new Teacher("Nelma", null, null);
        nelma.setInvalidDayOfWeeks(Set.of(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY));

        Teacher rodolfo = new Teacher("Rodolfo", null, null);
        rodolfo.setInvalidDayOfWeeks(Set.of(DayOfWeek.FRIDAY));

        Teacher vanessa = new Teacher("Vanessa", null, null);
        vanessa.setSpecificUnavailableDates(unavailableDateRange(LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 30),
                Set.of(LocalDate.of(2026, 7, 2), LocalDate.of(2026, 7, 9), LocalDate.of(2026, 7, 16),
                        LocalDate.of(2026, 8, 6), LocalDate.of(2026, 8, 13), LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 27))));

        Teacher alisson = new Teacher("Alisson", null, null);

        teacherRepository.saveAll(List.of(nelma, rodolfo, vanessa, alisson));
    }

    private static Set<LocalDate> unavailableDateRange(LocalDate start, LocalDate end, Set<LocalDate> extraDates) {
        Set<LocalDate> dates = new java.util.HashSet<>(extraDates);
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            dates.add(date);
        }
        return dates;
    }
}
