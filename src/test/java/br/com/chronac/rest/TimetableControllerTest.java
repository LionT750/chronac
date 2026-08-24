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
        properties = {"chronac.demo.seconds=5", "chronac.demo.unimproved-seconds=5"})
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

        // The exact shape ui/src/App.jsx reads. Lesson stopped being the planning
        // entity in favour of Block and is now materialized from it, so this is the
        // contract that keeps the calendar rendering - assert it rather than trust it.
        JsonNode lesson = solved.get("lessons").get(0);
        assertThat(lesson.get("teacher").isTextual()).isTrue();
        assertThat(lesson.get("subject").get("name").isTextual()).isTrue();
        assertThat(lesson.get("room").get("name").asText()).startsWith("Sala ");
        JsonNode timeslot = lesson.get("timeslot");
        assertThat(timeslot.get("date").asText()).matches("\\d{4}-\\d{2}-\\d{2}");
        assertThat(timeslot.get("dayOfWeek").isTextual()).isTrue();
        assertThat(timeslot.get("startTime").asText()).startsWith("18:40");
        assertThat(timeslot.get("endTime").asText()).startsWith("22:00");
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