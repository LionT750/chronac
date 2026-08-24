package br.com.chronac.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"timefold.solver.termination.spent-limit=2s"})
class TimetableControllerTest {

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

    @Test
    void getSayHeyMaster_returnsLegacyHello() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/sayHeyMaster", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Hey from master Lucas");
    }

    private JsonNode waitForSolvedTimetable() throws Exception {
        long deadline = System.currentTimeMillis() + 60_000L;
        while (System.currentTimeMillis() < deadline) {
            ResponseEntity<String> response = restTemplate.getForEntity("/api/timetable", String.class);
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