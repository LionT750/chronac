package br.com.chronac.demo;

import br.com.chronac.domain.Teacher;
import br.com.chronac.domain.Turma;
import br.com.chronac.domain.Turno;
import br.com.chronac.repository.TeacherRepository;
import br.com.chronac.repository.TurmaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Seeds the H2 database with the demo turmas on startup: "Tecnico em
 * Desenvolvimento de Sistemas" (Sala 114) and "Jovem Programador" (Sala 115),
 * each with its own valid weekdays and the teachers assigned to it, so
 * {@link TimetableDemoData} can derive each subject's valid weekdays from its
 * turma's room instead of a hardcoded per-subject list. Runs after
 * {@link TeacherDataSeeder}, since it links teachers by name.
 */
@Component
@Order(2)
public class TurmaDataSeeder implements ApplicationRunner {

    private final TurmaRepository turmaRepository;
    private final TeacherRepository teacherRepository;

    public TurmaDataSeeder(TurmaRepository turmaRepository, TeacherRepository teacherRepository) {
        this.turmaRepository = turmaRepository;
        this.teacherRepository = teacherRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (turmaRepository.count() > 0) {
            return;
        }

        Map<String, Teacher> teachersByName = teacherRepository.findAll().stream()
                .collect(Collectors.toMap(Teacher::getName, Function.identity()));

        Turma tecnicoEmDesenvolvimentoDeSistemas = new Turma("Tecnico em Desenvolvimento de Sistemas", Turno.NOTURNO, null);
        tecnicoEmDesenvolvimentoDeSistemas.setRoomName("Sala 114");
        tecnicoEmDesenvolvimentoDeSistemas.setValidWeekdays(Set.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        tecnicoEmDesenvolvimentoDeSistemas.setTeachers(
                teachersFor(teachersByName, "Nelma", "Vanessa", "Rodolfo", "Alisson"));

        Turma jovemProgramador = new Turma("Jovem Programador", Turno.NOTURNO, null);
        jovemProgramador.setRoomName("Sala 115");
        jovemProgramador.setValidWeekdays(Set.of(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        jovemProgramador.setTeachers(teachersFor(teachersByName, "Vanessa", "Rodolfo", "Alisson"));

        turmaRepository.saveAll(List.of(tecnicoEmDesenvolvimentoDeSistemas, jovemProgramador));
    }

    private static Set<Teacher> teachersFor(Map<String, Teacher> teachersByName, String... names) {
        Set<Teacher> teachers = new HashSet<>();
        for (String name : names) {
            Teacher teacher = teachersByName.get(name);
            if (teacher != null) {
                teachers.add(teacher);
            }
        }
        return teachers;
    }
}
