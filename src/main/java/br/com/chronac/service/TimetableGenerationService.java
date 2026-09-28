package br.com.chronac.service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import br.com.chronac.demo.TimetableDemoData;
import br.com.chronac.domain.GenerationStatus;
import br.com.chronac.domain.Timetable;
import br.com.chronac.exception.DuplicateGenerationException;
import br.com.chronac.exception.UnknownDemoException;
import br.com.chronac.exception.UnknownJobException;
import br.com.chronac.rest.dto.GenerationJobResponse;

/**
 * On-demand schedule generations exposed as SSE streams.
 *
 * <p>A generation is started via {@link #startGeneration(String, String)} and runs
 * asynchronously in the Timefold {@link SolverManager}; every time the solver finds a
 * better solution, a full snapshot of it is broadcast to all attached streams, so the
 * frontend can render the schedule as it is being formed. Streams can be detached and
 * re-attached at any time ({@link #attachStream(String)}) — the solve keeps running
 * server-side regardless of connected clients.
 *
 * <p>State is in-memory only: jobs are lost on restart and are not shared across
 * instances. Fine for the demo phase; persistent job state comes with the
 * persistence epic.
 */
@Service
public class TimetableGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableGenerationService.class);

    /** Demo identifier that reuses the same problem solved at server startup. */
    public static final String DEMO_MULTI_TURMA = "MULTI_TURMA_DEMO";

    private final SolverManager<Timetable> solverManager;
    private final ObjectMapper objectMapper;

    /** Minimum interval between two snapshot broadcasts for the same job. */
    private final long streamThrottleNanos;

    /** How long an attached stream may stay open. Must exceed the solver spent-limit. */
    private final long streamTimeoutMillis;

    /** The startup demo solve uses problem id 0L; generations get unique ids from here. */
    private final AtomicLong problemIdSequence = new AtomicLong(1L);

    private final Map<String, GenerationJob> jobs = new ConcurrentHashMap<>();
    private final Map<String, String> runningJobByOwner = new ConcurrentHashMap<>();

    public TimetableGenerationService(SolverManager<Timetable> solverManager,
            ObjectMapper objectMapper,
            @Value("${chronac.generation.stream-throttle-ms:100}") long streamThrottleMillis,
            @Value("${chronac.generation.stream-timeout-ms:120000}") long streamTimeoutMillis) {
        this.solverManager = solverManager;
        this.objectMapper = objectMapper;
        this.streamThrottleNanos = streamThrottleMillis * 1_000_000L;
        this.streamTimeoutMillis = streamTimeoutMillis;
    }

    /**
     * Starts a new generation for the given user. One running generation per user:
     * a second call while the first is still running is rejected with 409.
     */
    public GenerationJobResponse startGeneration(String ownerEmail, String demo) {
        String jobId = UUID.randomUUID().toString();
        String previous = runningJobByOwner.putIfAbsent(ownerEmail, jobId);
        if (previous != null && jobs.get(previous).status() == GenerationStatus.RUNNING) {
            throw new DuplicateGenerationException(
                    "User already has a running generation. jobId: " + previous);
        }

        try {
            Timetable problem = buildProblem(demo);
            GenerationJob job = new GenerationJob(jobId, ownerEmail, problem);
            jobs.put(jobId, job);

            SolverJob<Timetable> solverJob = solverManager.solveAndListen(
                    problemIdSequence.getAndIncrement(),
                    problem,
                    bestSolution -> onBestSolution(job, bestSolution));
            job.solverJob(solverJob);

            CompletableFuture.runAsync(() -> finalizeWhenDone(job, solverJob));
            return new GenerationJobResponse(jobId, GenerationStatus.RUNNING);
        } catch (RuntimeException e) {
            runningJobByOwner.remove(ownerEmail, jobId);
            jobs.remove(jobId);
            throw e;
        }
    }

    /**
     * Attaches a new SSE stream to a generation. The freshest snapshot is sent
     * immediately (resume path); if the generation is no longer running, the final
     * snapshot is sent and the stream closes right away. Detaching never cancels
     * the solve — a client that navigates away and comes back simply re-attaches.
     */
    public SseEmitter attachStream(String jobId) {
        GenerationJob job = jobs.get(jobId);
        if (job == null) {
            throw new UnknownJobException("Unknown generation jobId: " + jobId);
        }

        SseEmitter emitter = new SseEmitter(streamTimeoutMillis);
        job.emitters.add(emitter);
        Runnable detach = () -> job.emitters.remove(emitter);
        emitter.onCompletion(detach);
        emitter.onTimeout(() -> {
            detach.run();
            emitter.complete();
        });
        emitter.onError(e -> detach.run());

        String snapshot = job.latestSnapshotJson;
        if (snapshot != null) {
            sendTo(emitter, snapshot);
        }
        if (job.status() != GenerationStatus.RUNNING) {
            safeComplete(emitter);
        }
        return emitter;
    }

    private Timetable buildProblem(String demo) {
        if (DEMO_MULTI_TURMA.equals(demo)) {
            return TimetableDemoData.buildDemoProblem();
        }
        throw new UnknownDemoException("Unknown demo identifier: " + demo);
    }

    /**
     * Called by the solver thread on every new best solution. The snapshot is
     * serialized here (on the solver thread) so no mutable planning object is
     * ever shared across threads; only the immutable JSON travels onwards.
     */
    private void onBestSolution(GenerationJob job, Timetable bestSolution) {
        String snapshot = serialize(bestSolution);
        job.latestSnapshotJson = snapshot;

        long now = System.nanoTime();
        if (now - job.lastBroadcastNanos >= streamThrottleNanos) {
            job.lastBroadcastNanos = now;
            broadcast(job, snapshot);
        }
    }

    private void finalizeWhenDone(GenerationJob job, SolverJob<Timetable> solverJob) {
        try {
            Timetable finalSolution = solverJob.getFinalBestSolution();
            String snapshot = serialize(finalSolution);
            job.latestSnapshotJson = snapshot;
            job.status = GenerationStatus.FINISHED;
            broadcast(job, snapshot);
            completeEmitters(job);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failJob(job, e);
        } catch (ExecutionException e) {
            failJob(job, e.getCause());
        } finally {
            runningJobByOwner.remove(job.ownerEmail, job.jobId);
        }
    }

    private void failJob(GenerationJob job, Throwable cause) {
        LOGGER.error("Schedule generation {} failed.", job.jobId, cause);
        job.status = GenerationStatus.FAILED;
        completeEmitters(job);
    }

    private void broadcast(GenerationJob job, String snapshot) {
        for (SseEmitter emitter : job.emitters) {
            sendTo(emitter, snapshot);
        }
    }

    private void sendTo(SseEmitter emitter, String snapshot) {
        try {
            emitter.send(SseEmitter.event().data(snapshot));
        } catch (IOException | IllegalStateException e) {
            // Client went away; the emitter callbacks will detach it.
        }
    }

    private void completeEmitters(GenerationJob job) {
        for (SseEmitter emitter : job.emitters) {
            safeComplete(emitter);
        }
        job.emitters.clear();
    }

    private void safeComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (IllegalStateException e) {
            // Already completed by a callback.
        }
    }

    private String serialize(Timetable timetable) {
        try {
            return objectMapper.writeValueAsString(timetable);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize timetable snapshot.", e);
        }
    }

    private static final class GenerationJob {

        private final String jobId;
        private final String ownerEmail;
        private final Timetable problem;
        private volatile GenerationStatus status = GenerationStatus.RUNNING;
        private volatile String latestSnapshotJson;
        private volatile long lastBroadcastNanos;
        private volatile SolverJob<Timetable> solverJob;
        private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

        GenerationJob(String jobId, String ownerEmail, Timetable problem) {
            this.jobId = jobId;
            this.ownerEmail = ownerEmail;
            this.problem = problem;
        }

        void solverJob(SolverJob<Timetable> solverJob) {
            this.solverJob = solverJob;
        }

        GenerationStatus status() {
            return status;
        }
    }
}
