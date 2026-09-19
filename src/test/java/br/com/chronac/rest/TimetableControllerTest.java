package br.com.chronac.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"timefold.solver.termination.spent-limit=2s"})
@ActiveProfiles("dev")
class TimetableControllerTest {
    private String sessionCookie;

    @BeforeEach
    void authenticate() throws Exception {
        var csrf = restTemplate.getForEntity("/api/auth/csrf", String.class);
        var token = objectMapper.readTree(csrf.getBody());
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, csrf.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";", 2)[0]);
        headers.add(token.get("headerName").asText(), token.get("token").asText());
        var login = restTemplate.postForEntity("/api/auth/login", new HttpEntity<>(
                Map.of("email", "admin123@senac.com", "password", "admin1234"), headers), String.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        sessionCookie = login.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";", 2)[0];
    }

    private ResponseEntity<String> authenticatedGet(String path) {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionCookie);
        return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getTimetable_returnsSolvedTimetableAsJson() throws Exception {
        JsonNode solved = waitForSolvedTimetable();

        assertThat(solved.has("name")).isTrue();
        assertThat(solved.get("lessons").isArray()).isTrue();
        assertThat(solved.get("lessons").size()).isGreaterThan(0);
        assertThat(solved.get("score").isTextual()).isTrue();
        assertThat(solved.get("score").asText()).contains("hard");
    }

    private JsonNode waitForSolvedTimetable() throws Exception {
        long deadline = System.currentTimeMillis() + 60_000L;
        while (System.currentTimeMillis() < deadline) {
            ResponseEntity<String> response = authenticatedGet("/api/timetable");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            JsonNode node = objectMapper.readTree(response.getBody());
            if (node.has("score") && !node.get("score").isNull()) {
                return node;
            }
            Thread.sleep(500);
        }
        throw new AssertionError("Timed out waiting for the demo timetable to be solved.");
    }
}
