package br.com.chronac.rest;

import br.com.chronac.domain.Timetable;
import br.com.chronac.service.TimetableDemoSolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TimetableController {

    private final TimetableDemoSolver timetableDemoSolver;
    private final ObjectMapper objectMapper;

    public TimetableController(TimetableDemoSolver timetableDemoSolver, ObjectMapper objectMapper) {
        this.timetableDemoSolver = timetableDemoSolver;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/timetable")
    public Timetable getTimetable() {
        return timetableDemoSolver.getBestSolution();
    }

}
