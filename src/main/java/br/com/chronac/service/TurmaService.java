package br.com.chronac.service;

import br.com.chronac.domain.Teacher;
import br.com.chronac.domain.Turma;
import br.com.chronac.repository.TeacherRepository;
import br.com.chronac.repository.TurmaRepository;
import br.com.chronac.rest.dto.TurmaRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final TeacherRepository teacherRepository;

    public TurmaService(TurmaRepository turmaRepository, TeacherRepository teacherRepository) {
        this.turmaRepository = turmaRepository;
        this.teacherRepository = teacherRepository;
    }

    public List<Turma> findAll() {
        return turmaRepository.findAll();
    }

    public Turma findById(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma " + id + " not found"));
    }

    public Turma create(TurmaRequest request) {
        Turma turma = new Turma(request.name(), request.turno(), request.capacity());
        turma.setRoomName(request.roomName());
        turma.setValidWeekdays(request.validWeekdays());
        turma.setTeachers(resolveTeachers(request.teacherIds()));
        return turmaRepository.save(turma);
    }

    public Turma update(Long id, TurmaRequest request) {
        Turma turma = findById(id);
        turma.setName(request.name());
        turma.setTurno(request.turno());
        turma.setCapacity(request.capacity());
        turma.setRoomName(request.roomName());
        turma.setValidWeekdays(request.validWeekdays());
        turma.setTeachers(resolveTeachers(request.teacherIds()));
        return turmaRepository.save(turma);
    }

    public void delete(Long id) {
        if (!turmaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma " + id + " not found");
        }
        turmaRepository.deleteById(id);
    }

    private Set<Teacher> resolveTeachers(List<Long> teacherIds) {
        if (teacherIds == null || teacherIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Teacher> teachers = new HashSet<>(teacherRepository.findAllById(teacherIds));
        if (teachers.size() != new HashSet<>(teacherIds).size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more teacherIds do not exist");
        }
        return teachers;
    }
}
