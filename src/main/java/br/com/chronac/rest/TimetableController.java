package br.com.chronac.rest;

import br.com.chronac.demo.TimetableDemoData;
import br.com.chronac.domain.Timetable;
import br.com.chronac.service.TimetableDemoSolver;
import br.com.chronac.service.TimetableGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TimetableController {

    private final TimetableDemoSolver timetableDemoSolver;
    private final TimetableGenerator timetableGenerator;
    private final ObjectMapper objectMapper;

    public TimetableController(TimetableDemoSolver timetableDemoSolver, TimetableGenerator timetableGenerator,
            ObjectMapper objectMapper) {
        this.timetableDemoSolver = timetableDemoSolver;
        this.timetableGenerator = timetableGenerator;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/timetable")
    public Timetable getTimetable() {
        return timetableDemoSolver.getBestSolution();
    }

    /**
     * Fast, seedable generation for testing: solves the demo problem once with a
     * fixed random seed and returns the best solution found. Defaults
     * to the seed pinned for the demo, so calling it bare reproduces exactly what
     * GET /api/timetable serves.
     */
    @GetMapping("/generate")
    public Timetable generate(@RequestParam(required = false) Long seed,
            @RequestParam(required = false) Long seconds) {
        return timetableGenerator.solveSeeded(TimetableDemoData.buildDemoProblem(),
                seed == null ? TimetableGenerator.DEMO_SEED : seed,
                seconds == null ? TimetableGenerator.DEMO_BUDGET_SECONDS : seconds,
                TimetableGenerator.DEMO_UNIMPROVED_SECONDS);
    }

    /**
     * Scans seeds looking for a feasible ("perfect") real timetable. Whatever seed
     * this reports is what belongs in TimetableGenerator.DEMO_SEED.
     */
    @GetMapping("/find-perfect")
    public TimetableGenerator.GenerationResult findPerfect(@RequestParam(defaultValue = "14") int maxSeeds,
            @RequestParam(required = false) Long seconds) {
        return timetableGenerator.findPerfect(TimetableDemoData::buildDemoProblem, maxSeeds,
                seconds == null ? TimetableGenerator.DEMO_BUDGET_SECONDS : seconds);
    }

    @GetMapping("/sayHeyMaster")
    public String sayHeyMaster() throws JsonProcessingException {
        // Keep the legacy payload: a JSON string, so the UI can consume it with response.json().
        return objectMapper.writeValueAsString("Hey from master Lucas");
    }
}