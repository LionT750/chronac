package br.com.chronac.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.com.chronac.rest.dto.GenerationJobResponse;
import br.com.chronac.rest.dto.TimetableGenerateRequest;
import br.com.chronac.service.TimetableGenerationService;
import jakarta.validation.Valid;

/**
 * Starts on-demand schedule generations and streams their progress.
 *
 * <pre>
 * POST /api/v2/timetable/generate              → 202 { jobId, status }
 * GET  /api/v2/timetable/generate/{jobId}/stream → text/event-stream
 * </pre>
 *
 * <p>Each SSE event carries a full snapshot of the best solution found so far
 * (same shape as {@code GET /api/timetable}); the stream closes when the
 * generation finishes. Re-opening the stream with the same {@code jobId}
 * resumes from the freshest snapshot.
 */
@RestController
@RequestMapping("/api/v2/timetable/generate")
public class TimetableGenerationController {

    private final TimetableGenerationService generationService;

    public TimetableGenerationController(TimetableGenerationService generationService) {
        this.generationService = generationService;
    }

    @PostMapping
    public ResponseEntity<GenerationJobResponse> generate(
            @RequestBody @Valid TimetableGenerateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        GenerationJobResponse response = generationService.startGeneration(jwt.getSubject(), request.demo());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{jobId}/stream")
    public SseEmitter stream(@PathVariable String jobId) {
        return generationService.attachStream(jobId);
    }
}
