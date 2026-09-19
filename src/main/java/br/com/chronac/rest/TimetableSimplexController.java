package br.com.chronac.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.chronac.domain.Timetable;
import br.com.chronac.exception.NotImplementedException;
import br.com.chronac.service.SimplexService;

import org.springframework.web.bind.annotation.GetMapping;


@RestController 
@RequestMapping("/api/v2")

public class TimetableSimplexController {
    
    private final SimplexService simplexService;

    public TimetableSimplexController(SimplexService simplexService) {
        this.simplexService = simplexService;
    }

    @GetMapping("timetable")
    public Timetable getTimetable() {
        throw new NotImplementedException("This endpoint has not wired funcionality yet");
    }
        
}
