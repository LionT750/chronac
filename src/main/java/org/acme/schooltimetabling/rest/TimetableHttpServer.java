package org.acme.schooltimetabling.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.acme.schooltimetabling.domain.Timetable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Minimal HTTP server (JDK built-in, no framework) exposing the solved
 * timetable as JSON at GET /api/timetable.
 */
public class TimetableHttpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableHttpServer.class);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final Timetable timetable;

    public TimetableHttpServer(Timetable timetable) {
        this.timetable = timetable;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/timetable", this::handleGetTimetable);
        server.start();
        LOGGER.info("REST API listening on http://localhost:{}/api/timetable", port);
    }

    private void handleGetTimetable(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }
        byte[] responseBytes = objectMapper.writeValueAsBytes(timetable);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
