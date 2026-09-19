package br.com.chronac.service;

import br.com.chronac.domain.Teacher;
import br.com.chronac.repository.TeacherRepository;
import br.com.chronac.rest.dto.TeacherRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public List<Teacher> findAll() {
        return teacherRepository.findAll();
    }

    public Teacher findById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher " + id + " not found"));
    }

    public Teacher create(TeacherRequest request) {
        Teacher teacher = new Teacher(request.name(), request.email(), request.phone());
        teacher.setInvalidDayOfWeeks(request.invalidDayOfWeeks());
        teacher.setSpecificUnavailableDates(request.specificUnavailableDates());
        return teacherRepository.save(teacher);
    }

    public Teacher update(Long id, TeacherRequest request) {
        Teacher teacher = findById(id);
        teacher.setName(request.name());
        teacher.setEmail(request.email());
        teacher.setPhone(request.phone());
        teacher.setInvalidDayOfWeeks(request.invalidDayOfWeeks());
        teacher.setSpecificUnavailableDates(request.specificUnavailableDates());
        return teacherRepository.save(teacher);
    }

    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher " + id + " not found");
        }
        teacherRepository.deleteById(id);
    }
}
