package br.com.chronac.rest;

import br.com.chronac.domain.Turma;
import br.com.chronac.rest.dto.TurmaRequest;
import br.com.chronac.service.TurmaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @GetMapping
    public List<Turma> findAll() {
        return turmaService.findAll();
    }

    @GetMapping("/{id}")
    public Turma findById(@PathVariable Long id) {
        return turmaService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Turma create(@RequestBody TurmaRequest request) {
        return turmaService.create(request);
    }

    @PutMapping("/{id}")
    public Turma update(@PathVariable Long id, @RequestBody TurmaRequest request) {
        return turmaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        turmaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
