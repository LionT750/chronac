package br.com.chronac.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.chronac.domain.GenerationStatus;
import br.com.chronac.rest.dto.GenerationJobResponse;
import br.com.chronac.service.TimetableGenerationService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"timefold.solver.termination.spent-limit=2s"})
@ActiveProfiles("dev")
class TimetableGenerationControllerTest {

    private static final String GENERATE_PATH = "/api/v2/timetable/generate";
    private static final String DEMO = TimetableGenerationService.DEMO_MULTI_TURMA;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String sessionCookie;
    private String csrfCookie;
    private String csrfHeaderName;
    private String csrfToken;

    @BeforeEach
    void authenticate() throws Exception {
        var csrf = restTemplate.getForEntity("/api/auth/csrf", String.class);
        var token = objectMapper.readTree(csrf.getBody());
        csrfCookie = csrf.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";", 2)[0];
        csrfHeaderName = token.get("headerName").asText();
        csrfToken = token.get("token").asText();

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, csrfCookie);
        headers.add(csrfHeaderName, csrfToken);
        var login = restTemplate.postForEntity("/api/auth/login", new HttpEntity<>(
                Map.of("email", "admin123@senac.com", "password", "admin1234"), headers), String.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        sessionCookie = login.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";", 2)[0];
    }

    @Test
    void generate_withoutAuth_returns401() {
        // Send the CSRF token so the request passes the CSRF filter and reaches
        // the authentication check (an unauthenticated POST without it gets 403).
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, csrfCookie);
        headers.add(csrfHeaderName, csrfToken);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> response = restTemplate.postForEntity(
                GENERATE_PATH, new HttpEntity<>(Map.of("demo", DEMO), headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void generate_withUnknownDemo_returns400() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                GENERATE_PATH,
                new HttpEntity<>(Map.of("demo", "NO_SUCH_DEMO"), authenticatedHeaders()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void generate_withBlankDemo_returns400() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                GENERATE_PATH,
                new HttpEntity<>(Map.of("demo", ""), authenticatedHeaders()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void generate_secondPostWhileRunning_returns409() {
        String jobId = startGeneration();

        ResponseEntity<String> response = restTemplate.postForEntity(
                GENERATE_PATH,
                new HttpEntity<>(Map.of("demo", DEMO), authenticatedHeaders()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Wait for the generation to finish so the shared in-memory registry is
        // clean for the next test.
        List<String> events = new ArrayList<>();
        readStream(jobId, events, new CountDownLatch(0));
        assertThat(events).isNotEmpty();
    }

    @Test
    void stream_unknownJob_returns404() {
        ResponseEntity<String> response = restTemplate.exchange(
                GENERATE_PATH + "/does-not-exist/stream",
                HttpMethod.GET,
                new HttpEntity<>(authenticatedHeaders()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void generate_streamsSnapshotsUntilGenerationFinishes_thenResumesFinished() throws Exception {
        String jobId = startGeneration();

        List<String> events = new ArrayList<>();
        readStream(jobId, events, new CountDownLatch(0));

        // readStream only returns when the stream closes, i.e. when the generation finished.
        assertThat(events).isNotEmpty();
        JsonNode finalSnapshot = objectMapper.readTree(events.get(events.size() - 1));
        assertThat(finalSnapshot.has("name")).isTrue();
        assertThat(finalSnapshot.get("lessons").isArray()).isTrue();
        assertThat(finalSnapshot.get("score").isTextual()).isTrue();

        // Resume after finish: the final snapshot is delivered immediately and the
        // stream closes on the spot.
        List<String> resumed = new ArrayList<>();
        readStream(jobId, resumed, new CountDownLatch(0));

        assertThat(resumed).isNotEmpty();
        assertThat(objectMapper.readTree(resumed.get(resumed.size() - 1)).get("score"))
                .isEqualTo(finalSnapshot.get("score"));
    }

    @Test
    void stream_multipleClientsReceiveSnapshotsWhileRunning() throws Exception {
        String jobId = startGeneration();

        CountDownLatch clientAReceived = new CountDownLatch(1);
        List<String> eventsA = Collections.synchronizedList(new ArrayList<>());
        Thread clientA = new Thread(() -> readStream(jobId, eventsA, clientAReceived), "client-a");
        clientA.start();
        assertThat(clientAReceived.await(15, TimeUnit.SECONDS))
                .as("first client should receive live snapshots").isTrue();

        // A second client attaching while the generation is still running (the user
        // coming back to the tab) immediately receives the current best snapshot.
        CountDownLatch clientBReceived = new CountDownLatch(1);
        List<String> eventsB = Collections.synchronizedList(new ArrayList<>());
        Thread clientB = new Thread(() -> readStream(jobId, eventsB, clientBReceived), "client-b");
        clientB.start();
        assertThat(clientBReceived.await(15, TimeUnit.SECONDS))
                .as("resuming client should receive the current snapshot").isTrue();

        clientA.join(15_000);
        clientB.join(15_000);
        assertThat(eventsA).isNotEmpty();
        assertThat(eventsB).isNotEmpty();
    }

    private HttpHeaders authenticatedHeaders() {
        var headers = new HttpHeaders();
        // Both cookies, like a browser would send: XSRF-TOKEN for the CSRF filter,
        // the session cookie for authentication.
        headers.add(HttpHeaders.COOKIE, csrfCookie);
        headers.add(HttpHeaders.COOKIE, sessionCookie);
        headers.add(csrfHeaderName, csrfToken);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private String startGeneration() {
        ResponseEntity<GenerationJobResponse> response = restTemplate.postForEntity(
                GENERATE_PATH,
                new HttpEntity<>(Map.of("demo", DEMO), authenticatedHeaders()),
                GenerationJobResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(GenerationStatus.RUNNING);
        return response.getBody().jobId();
    }

    /**
     * Reads an SSE stream until the server closes it, collecting every {@code data:}
     * frame. Returns normally only when the stream closes.
     */
    private void readStream(String jobId, List<String> events, CountDownLatch onFirstEvent) {
        restTemplate.execute(
                GENERATE_PATH + "/" + jobId + "/stream",
                HttpMethod.GET,
                request -> request.getHeaders().add(HttpHeaders.COOKIE, sessionCookie),
                response -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data:")) {
                                events.add(line.substring(5).strip());
                                onFirstEvent.countDown();
                            }
                        }
                    }
                    return null;
                });
    }
}
